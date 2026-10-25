package com.rainccup.aktool.core.update

import com.rainccup.aktool.core.platform.UpdatePort
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 版本号解析与比较：发布 tag 可能带 `v` 前缀、预发布后缀，比较必须只看数字段。 */
class AppVersionTest {

    @Test
    fun parsesPlainAndPrefixedTags() {
        assertEquals("2.0.5", AppVersion.parse("2.0.5")?.toString())
        assertEquals("2.0.5", AppVersion.parse("v2.0.5")?.toString())
        assertEquals("2.0.5", AppVersion.parse("V2.0.5")?.toString())
        assertEquals("2.0.5", AppVersion.parse("  v2.0.5  ")?.toString())
    }

    @Test
    fun keepsPreReleaseSuffixAndStripsBuildMetadata() {
        assertEquals("2.1.0-rc1", AppVersion.parse("v2.1.0-rc1")?.toString())
        assertEquals("2.0.5", AppVersion.parse("2.0.5+build.7")?.toString())
    }

    @Test
    fun rejectsGarbage() {
        assertNull(AppVersion.parse(""))
        assertNull(AppVersion.parse("nightly"))
        assertNull(AppVersion.parse("v"))
        assertNull(AppVersion.parse("latest"))
    }

    @Test
    fun comparesEveryNumericSegment() {
        assertTrue(version("2.0.5") > version("2.0.4"))
        assertTrue(version("2.1.0") > version("2.0.9"))
        assertTrue(version("3.0.0") > version("2.99.99"))
        // 段数不一致时按缺省 0 补齐，"2.1" 与 "2.1.0" 视为同一版
        assertEquals(0, version("2.1").compareTo(version("2.1.0")))
        assertTrue(version("2.1.1") > version("2.1"))
    }

    @Test
    fun releaseOutranksPreReleaseOfSameNumbers() {
        assertTrue(version("2.0.5") > version("2.0.5-rc1"))
    }

    @Test
    fun picksAssetByPlatformExtension() {
        val repository = UpdateRepository(
            clientProvider = { error("下载路径不在本测试范围内") },
            port = FakePort(listOf("apk", "zip")),
        )
        val release = GithubRelease(
            tagName = "v2.0.6",
            body = "notes",
            assets = listOf(
                GithubAsset("AK-Tool_windows_v2.0.6.zip", "https://example.com/a.zip", 10),
                // apk 排在后面，但扩展名优先级更高，应该被选中
                GithubAsset("AK-Tool_android_v2.0.6.apk", "https://example.com/a.apk", 20),
                GithubAsset("notes.txt", "https://example.com/n.txt", 1),
            ),
        )

        assertEquals("AK-Tool_android_v2.0.6.apk", repository.pickAsset(release)?.name)
        assertEquals("2.0.6", release.version?.toString())
    }

    @Test
    fun returnsNullWhenNoAssetMatches() {
        val repository = UpdateRepository(
            clientProvider = { error("不会下载") },
            port = FakePort(listOf("msi")),
        )
        val release = GithubRelease(
            tagName = "v2.0.6",
            body = "",
            assets = listOf(GithubAsset("AK-Tool.apk", "https://example.com/a.apk", 1)),
        )

        assertNull(repository.pickAsset(release))
    }

    private fun version(raw: String): AppVersion = requireNotNull(AppVersion.parse(raw))

    private class FakePort(override val packageExtensions: List<String>) : UpdatePort {
        override fun canInstall(): Boolean = true
        override fun requestInstallPermission() = Unit
        override fun install(filePath: String): Boolean = true
        override suspend fun savePackage(
            fileName: String,
            channel: ByteReadChannel,
            totalBytes: Long,
            onProgress: (receivedBytes: Long) -> Unit,
        ): String? = null
    }
}
