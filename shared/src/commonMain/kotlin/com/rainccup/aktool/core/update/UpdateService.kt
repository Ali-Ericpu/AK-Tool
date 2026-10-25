package com.rainccup.aktool.core.update

import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.common.AppInfo
import com.rainccup.aktool.core.platform.AppScope
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.platform.UpdatePort
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.update_available_message
import com.rainccup.aktool.resources.update_check_failed
import com.rainccup.aktool.resources.update_download_failed
import com.rainccup.aktool.resources.update_install_failed
import com.rainccup.aktool.resources.update_no_asset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.getString

/**
 * 自动更新的唯一编排点：检查 → 下载 → 安装。
 *
 * 状态挂在应用级单例上，所以用户切页面、开关弹窗都不会打断正在进行的下载；
 * 启动时静默检查一次，发现新版本只用 snackbar 提示，不打断用户。
 */
class UpdateService(
    private val repository: UpdateRepository,
    private val port: UpdatePort,
    private val appScope: AppScope,
    private val messenger: Messenger,
    private val appInfo: AppInfo,
) {
    val state: StateFlow<UpdateUiState>
        field = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)

    private val mutex = Mutex()
    private var startupChecked = false

    /** 每个进程只跑一次，避免每次回到前台都打一次 GitHub API。 */
    fun checkOnStartup() {
        if (startupChecked) return
        startupChecked = true
        appScope.launch { check(manual = false) }
    }

    fun checkInBackground() {
        appScope.launch { check(manual = true) }
    }

    fun downloadInBackground() {
        appScope.launch { download() }
    }

    fun installInBackground() {
        appScope.launch { install() }
    }

    fun requestInstallPermission() = port.requestInstallPermission()

    /** 用户在系统设置里授予「安装未知应用」后回到设置页时，接着把已下载的包装掉。 */
    fun installIfPermitted() {
        appScope.launch {
            if (port.canInstall()) install()
        }
    }

    fun changeUpdateDialogState() {
        val value = state.value
        if (value is UpdateUiState.Available) {
            state.value = value.copy(show = !value.show)
        }
    }

    fun dismiss() {
        when (val value = state.value) {
            // 「稍后」只是收起对话框：可用更新留着，设置行还能再打开
            is UpdateUiState.Available -> state.value = value.copy(show = false)

            is UpdateUiState.UpToDate,
            is UpdateUiState.Failed,
            is UpdateUiState.NeedPermission,
            -> state.value = UpdateUiState.Idle

            else -> Unit
        }
    }

    private suspend fun check(manual: Boolean) = mutex.withLock {
        val current = AppVersion.parse(appInfo.versionName) ?: return@withLock
        if (state.value is UpdateUiState.Checking || state.value is UpdateUiState.Downloading) return@withLock

        state.value = UpdateUiState.Checking
        val attempt = runCatching { repository.latestRelease() }
            .onFailure { Logger.w { "checkUpdateFail: ${it.message}" } }
        val release = attempt.getOrNull()
        if (release == null) {
            state.value = if (manual) {
                UpdateUiState.Failed(
                    getString(
                        Res.string.update_check_failed,
                        attempt.exceptionOrNull()?.message.orEmpty(),
                    )
                )
            } else {
                UpdateUiState.Idle
            }
            return@withLock
        }

        val latest = release.version
        if (latest == null) {
            state.value = if (manual) {
                UpdateUiState.Failed(getString(Res.string.update_check_failed, release.tagName))
            } else {
                UpdateUiState.Idle
            }
            return@withLock
        }
        if (latest <= current) {
            state.value = UpdateUiState.UpToDate(appInfo.versionName)
            return@withLock
        }

        val asset = repository.pickAsset(release)
        state.value = UpdateUiState.Available(
            show = manual,
            version = latest.toString(),
            notes = release.body.trim(),
            assetUrl = asset?.downloadUrl.orEmpty(),
            assetName = asset?.name.orEmpty(),
            sizeBytes = asset?.size ?: 0L,
            assetId = asset?.id ?: 0L,
        )
        if (!manual) messenger.show(getString(Res.string.update_available_message, latest.toString()))
    }

    private suspend fun download() {
        val available = state.value as? UpdateUiState.Available ?: return
        if (available.assetUrl.isBlank()) {
            state.value = UpdateUiState.Failed(getString(Res.string.update_no_asset))
            return
        }
        // 先下包再谈权限：安装授权只影响最后一步，下载失败比授权失败更该被看见
        val total = available.sizeBytes
        state.value = UpdateUiState.Downloading(available.version, 0f, 0L, total)
        val fileName = available.assetName.ifBlank { "AK-Tool-${available.version}" }
            .replace('/', '_')
            .replace('\\', '_')

        val path = try {
            repository.download(
                asset = GithubAsset(
                    id = available.assetId,
                    name = available.assetName,
                    downloadUrl = available.assetUrl,
                    size = available.sizeBytes,
                ),
                fileName = fileName,
            ) { received, totalBytes ->
                val progress = if (totalBytes > 0) (received.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
                state.value = UpdateUiState.Downloading(available.version, progress, received, totalBytes)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // 应用级协程里抛出去就是进程崩溃，这里必须兜住
            Logger.w { "updateDownloadCrashed: ${e.message}" }
            null
        }

        if (path == null) {
            state.value = UpdateUiState.Failed(getString(Res.string.update_download_failed))
            return
        }
        state.value = UpdateUiState.Ready(available.version, path)
        install()
    }

    private suspend fun install() {
        // 路径可能来自「下载完直接装」的 Ready，也可能是授权回来后重试的 NeedPermission
        val pending = when (val current = state.value) {
            is UpdateUiState.Ready -> current.version to current.path
            is UpdateUiState.NeedPermission -> current.version to current.path
            else -> return
        }
        val (version, path) = pending
        if (path.isBlank()) return
        if (!port.canInstall()) {
            state.value = UpdateUiState.NeedPermission(version, path)
            return
        }
        val installed = try {
            port.install(path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.w { "updateInstallCrashed: ${e.message}" }
            false
        }
        if (!installed) {
            state.value = UpdateUiState.Failed(getString(Res.string.update_install_failed))
            return
        }
        // 会话已交给系统，安装确认由系统弹窗接管；关掉自己的弹窗，避免挡在后面
        state.value = UpdateUiState.Idle
    }
}
