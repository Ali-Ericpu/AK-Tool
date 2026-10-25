package com.rainccup.aktool.core.update

/** 自动更新的界面状态。UI 只读它，动作全部走 [UpdateService]。 */
sealed interface UpdateUiState {

    /** 什么都没发生（或者用户关掉了弹窗）。 */
    data object Idle : UpdateUiState

    data object Checking : UpdateUiState

    data class UpToDate(val version: String) : UpdateUiState

    /** 有可用新版本。[assetUrl] 为空表示 release 里没找到当前平台能装的包。 */
    data class Available(
        val version: String,
        val notes: String,
        val assetUrl: String,
        val assetName: String,
        val sizeBytes: Long,
        val assetId: Long = 0L,
    ) : UpdateUiState

    data class Downloading(
        val version: String,
        val progress: Float,
        val receivedBytes: Long,
        val totalBytes: Long,
    ) : UpdateUiState

    /** 安装包已经就绪并且即将拉起安装器。 */
    data class Ready(val version: String, val path: String) : UpdateUiState

    /** Android 没有「安装未知应用」授权，需要用户去系统设置里开。 */
    data class NeedPermission(val version: String, val path: String) : UpdateUiState

    data class Failed(val message: String) : UpdateUiState
}
