package com.rainccup.aktool.core.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GitHub `GET /repos/{owner}/{repo}/releases/latest` 的响应子集。 */
@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String = "",
    val name: String = "",
    val body: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    val prerelease: Boolean = false,
    val draft: Boolean = false,
    val assets: List<GithubAsset> = emptyList(),
) {
    val version: AppVersion? get() = AppVersion.parse(tagName)
}

@Serializable
data class GithubAsset(
    val name: String = "",
    @SerialName("browser_download_url") val downloadUrl: String = "",
    val size: Long = 0L,
    /** 走 api.github.com 资产端点下载时要用（见 [UpdateConfig.assetApiUrl]）。 */
    val id: Long = 0L,
)

/** 更新源地址只在这里维护一处。 */
object UpdateConfig {
    const val OWNER: String = "Ali-Ericpu"
    const val REPO: String = "AK-Tool"
    const val API_LATEST_RELEASE: String =
        "https://api.github.com/repos/$OWNER/$REPO/releases/latest"

    /**
     * 资产下载端点。带 `Accept: application/octet-stream` 请求时它会 302 到 CDN，
     * 走的是 api.github.com 这条线路——github.com 本身在某些网络下连不上（TCP 直接超时），
     * 但 api.github.com 通常可达，所以优先用这条。
     */
    fun assetApiUrl(assetId: Long): String =
        "https://api.github.com/repos/$OWNER/$REPO/releases/assets/$assetId"

    /** GitHub 要求带 User-Agent，否则返回 403。 */
    const val USER_AGENT: String = "AK-Tool-Updater"
}
