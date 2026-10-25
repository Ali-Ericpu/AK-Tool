package com.rainccup.aktool.core.update

import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.common.JsonUtil
import com.rainccup.aktool.core.platform.UpdatePort
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

/**
 * 跟 GitHub Releases 打交道：查最新版、挑当前平台能装的包、流式下载。
 *
 * 用的是独立的 [createUpdateHttpClient] 实例，而不是应用那个带 baseUrl/JSON 默认头的客户端：
 * 下载链路需要单独约束引擎（Android 固定 HTTP/1.1，见 [createUpdateHttpClient] 的说明）。
 */
class UpdateRepository(
    private val clientProvider: () -> HttpClient,
    private val port: UpdatePort,
) {

    suspend fun latestRelease(): GithubRelease {
        val payload = clientProvider().get(UpdateConfig.API_LATEST_RELEASE) {
            header(HttpHeaders.Accept, "application/vnd.github+json")
            header(HttpHeaders.UserAgent, UpdateConfig.USER_AGENT)
        }.bodyAsText()
        return JsonUtil.json.decodeFromString(payload)
    }

    /**
     * 按平台偏好挑包：Android 只要 apk，桌面优先 msi、退回 exe。
     * 找不到时返回 null（release 可能只传了 zip）。
     */
    fun pickAsset(release: GithubRelease): GithubAsset? {
        port.packageExtensions.forEach { extension ->
            release.assets.firstOrNull { it.name.endsWith(".$extension", ignoreCase = true) }
                ?.let { return it }
        }
        return null
    }

    /**
     * 流式下载到平台目录，[onProgress] 回调 (已下载, 总大小)；失败返回 null。
     *
     * 依次尝试 [UpdateConfig.assetApiUrl]（走 api.github.com，github.com 被墙时这条仍连通）和直链，
     * 前一条的连接/超时异常在这里吞掉并记日志——调用方跑在应用级协程里，抛出会直接拉崩进程。
     */
    suspend fun download(
        asset: GithubAsset,
        fileName: String,
        onProgress: (received: Long, total: Long) -> Unit,
    ): String? {
        for (url in downloadUrls(asset)) {
            val attempt = runCatching {
                clientProvider().prepareGet(url) {
                    header(HttpHeaders.UserAgent, UpdateConfig.USER_AGENT)
                    if (url != asset.downloadUrl) {
                        header(HttpHeaders.Accept, "application/octet-stream")
                    }
                }.execute { response ->
                    if (!response.status.isSuccess()) {
                        error("HTTP ${response.status.value} from $url")
                    }
                    // Ktor 3 去掉了 HttpResponse.contentLength()，直接读响应头更稳
                    val total = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: asset.size
                    port.savePackage(fileName, response.bodyAsChannel(), total) { received ->
                        onProgress(received, total)
                    }
                }
            }
            attempt.getOrNull()?.let { return it }
            Logger.w { "updateDownloadFail url=$url cause=${attempt.exceptionOrNull()?.message}" }
        }
        return null
    }

    /** api.github.com 的资产端点排前面，直链兜底。 */
    private fun downloadUrls(asset: GithubAsset): List<String> = buildList {
        if (asset.id > 0L) add(UpdateConfig.assetApiUrl(asset.id))
        if (asset.downloadUrl.isNotBlank()) add(asset.downloadUrl)
    }.distinct()
}
