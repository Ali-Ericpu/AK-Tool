package com.rainccup.aktool.core.platform

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidUpdatePort(private val context: Context) : UpdatePort {

    override val packageExtensions: List<String> = listOf("apk")

    override fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    override fun requestInstallPermission() {
        runCatching {
            val intent =
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:${context.packageName}".toUri(),
                )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override suspend fun savePackage(
        fileName: String,
        channel: ByteReadChannel,
        totalBytes: Long,
        onProgress: (Long) -> Unit,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            // 只保留最近一次下载：旧安装包没有任何用处，还占几十 MB
            val dir = File(context.filesDir, "data/updates").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }

            val target = File(dir, fileName)
            val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
            var received = 0L
            target.outputStream().use { out ->
                while (true) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read < 0) break
                    if (read == 0) continue
                    out.write(buffer, 0, read)
                    received += read
                    onProgress(received)
                }
            }

            // 落盘校验：空文件、长度不足、或者是 JSON/HTML 错误页，都当失败——否则会一路带到安装器
            val head = ByteArray(1)
            target.inputStream().use { it.read(head) }
            val corrupt = target.length() == 0L ||
                (totalBytes > 0 && target.length() < totalBytes) ||
                head[0] == '{'.code.toByte() ||
                head[0] == '<'.code.toByte()
            if (corrupt) {
                Logger.w {
                    "updatePackageCorrupt: len=${target.length()} expected=$totalBytes head=${head[0].toInt().toChar()}"
                }
                target.delete()
                null
            } else {
                target.absolutePath
            }
        }.getOrNull()
    }

    /**
     * 走 PackageInstaller 会话，而不是 `ACTION_VIEW` + FileProvider：
     *
     * - 不再需要 content:// 中转，也就没有 FileProvider / androidx.core 依赖；
     * - 系统自己弹出安装确认，完成后通过 [UpdateInstallReceiver] 回报状态；
     * - 返回 true 只代表会话已提交，真正结果由系统异步回调。
     */
    @SuppressLint("RequestInstallPackagesPolicy")
    override fun install(filePath: String): Boolean = runCatching {
        val apk = File(filePath)
        if (!apk.isFile || apk.length() == 0L) return@runCatching false

        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL,
        ).apply {
            setAppPackageName(context.packageName)
            runCatching {
                setAppLabel(context.applicationInfo.loadLabel(context.packageManager).toString())
            }
        }

        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                // 会话写入的长度必须与 apk 实际大小一致，否则 commit 会报 STATUS_FAILURE
                session.openWrite(WRITE_NAME, 0, apk.length()).use { out ->
                    input.copyTo(out, DOWNLOAD_BUFFER_SIZE)
                    session.fsync(out)
                }
            }
            session.commit(installCallback(sessionId))
        }
        true
    }.onFailure { Logger.w { "updateInstallFail: ${it.message}" } }.getOrDefault(false)

    private fun installCallback(sessionId: Int) =
        PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(context, UpdateInstallReceiver::class.java)
                .putExtra(PackageInstaller.EXTRA_SESSION_ID, sessionId),
            // 系统要往回调 Intent 里塞 status 与结果 Intent，所以 S+ 必须 MODIABLE(MUTABLE)
            PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag(),
        ).intentSender

    private fun mutableFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0

    private companion object {
        const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
        const val WRITE_NAME = "ak-tool-update.apk"
    }
}

/**
 * PackageInstaller 的会话结果回调。
 *
 * 低于 Android 12 的设备会先回 [PackageInstaller.STATUS_PENDING_USER_ACTION]，需要由我们
 * 启动系统给的确认界面；新版系统提交会话后直接弹窗，这里只处理成功/失败。
 */
class UpdateInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE,
        )
        when (status) {
            PackageInstaller.STATUS_SUCCESS -> Logger.i { "updateInstalled" }

            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = intent.parcelableExtra(Intent.EXTRA_INTENT) ?: return
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(confirm) }
                    .onFailure { Logger.w { "updateConfirmFail: ${it.message}" } }
            }

            else -> Logger.w {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()
                "updateInstallFailed: status=$status $message"
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.parcelableExtra(name: String): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, Intent::class.java)
        } else {
            getParcelableExtra(name)
        }
}

actual fun createUpdatePort(): UpdatePort = AndroidUpdatePort(AndroidContextHolder.appContext)
