package com.rainccup.aktool.core.platform

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

class DesktopUpdatePort : UpdatePort {

    override val packageExtensions: List<String> = listOf("msi", "exe")

    override fun canInstall(): Boolean = true

    override fun requestInstallPermission() = Unit

    override suspend fun savePackage(
        fileName: String,
        channel: ByteReadChannel,
        totalBytes: Long,
        onProgress: (Long) -> Unit,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(System.getProperty("java.io.tmpdir"), "ak-tool-updates").apply { mkdirs() }
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

            // 落盘校验：空文件、长度不足、或者是 JSON/HTML 错误页，都当失败
            val head = ByteArray(1)
            target.inputStream().use { it.read(head) }
            val corrupt = target.length() == 0L ||
                (totalBytes > 0 && target.length() < totalBytes) ||
                head[0] == '{'.code.toByte() ||
                head[0] == '<'.code.toByte()
            if (corrupt) {
                target.delete()
                null
            } else {
                target.absolutePath
            }
        }.getOrNull()
    }

    override fun install(filePath: String): Boolean = runCatching {
        val file = File(filePath)
        if (!file.exists() || !Desktop.isDesktopSupported()) return@runCatching false
        Desktop.getDesktop().open(file)
        true
    }.getOrDefault(false)

    private companion object {
        const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
    }
}

actual fun createUpdatePort(): UpdatePort = DesktopUpdatePort()
