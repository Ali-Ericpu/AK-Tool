package com.rainccup.aktool.core.platform

import io.ktor.utils.io.ByteReadChannel

/**
 * 自动更新需要的平台能力：下载落盘、安装授权、拉起系统安装器。
 *
 * 两端都是 JVM，但文件落点（应用私有目录 / 临时目录）与安装动作（Intent / Desktop.open）
 * 完全不同，所以拆成平台实现，common 只负责编排流程。
 */
interface UpdatePort {
    /** 该平台可安装包的扩展名，按优先级排列——用来从 release 资源里挑包。 */
    val packageExtensions: List<String>

    /** 是否已获得安装授权。桌面恒为 true；Android 需要「安装未知应用」授权。 */
    fun canInstall(): Boolean

    /** 引导用户去系统设置里授权。 */
    fun requestInstallPermission()

    /** 把响应流写到磁盘，返回最终文件路径；失败返回 null。 */
    suspend fun savePackage(
        fileName: String,
        channel: ByteReadChannel,
        totalBytes: Long,
        onProgress: (receivedBytes: Long) -> Unit,
    ): String?

    /** 拉起系统安装器；返回 false 表示没能启动（通常会退化成"让用户手动打开文件"）。 */
    fun install(filePath: String): Boolean
}

expect fun createUpdatePort(): UpdatePort
