package com.rainccup.aktool.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 扫描 shared 源码的 import，断言包间依赖只允许出现在 allowedEdges 里。
 *
 * 包级分层（而非 Gradle 模块）无法靠编译器强制依赖方向，这个测试是唯一的强制手段：
 * 初始允许列表 = 迁移前的现状（先绿），随后逐步收紧到 NiA 目标态。
 */
class ArchitectureRuleTest {

    @Test
    fun packageDependenciesStayWithinAllowedEdges() {
        val violations = collectEdges()
            .filterNot { (from, to) -> isAllowed(from, to) }
            .map { (from, to) -> "$from -> $to" }
            .distinct()
            .sorted()

        assertTrue(
            violations.isEmpty(),
            buildString {
                appendLine("Package dependency violations (register in ArchitectureRuleTest.allowedEdges, or fix the code):")
                violations.forEach { appendLine("  $it") }
            },
        )
    }

    /** 打印当前所有边，供维护 allowedEdges 使用（不参与断言）。 */
    @Test
    fun printCurrentEdges() {
        val edges = collectEdges().map { (from, to) -> "$from -> $to" }.distinct().sorted()
        println("=== current package edges (${edges.size}) ===")
        edges.forEach { println("  \"" + it + "\",") }
    }

    private fun isAllowed(from: String, to: String): Boolean =
        from == to || to == "resources" || allowedEdges.any { it.first == from && it.second == to }

    private fun collectEdges(): List<Pair<String, String>> {
        val root = findRepoRoot() ?: return emptyList()
        val sourceRoot = File(root, "shared/src")
        if (!sourceRoot.isDirectory) return emptyList()
        return sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val ownLayer = layerOf(file) ?: return@flatMap emptyList()
                file.readLines()
                    .filter { it.startsWith("import com.rainccup.aktool.") }
                    .mapNotNull { targetLayerOf(it.removePrefix("import ").trim()) }
                    .map { ownLayer to it }
            }
            .toList()
    }

    /** 文件 → 它所属的层（取 aktool 之后的部分，保留两级以便区分 core.xxx 与 feature.xxx）。 */
    private fun layerOf(file: File): String? {
        val pkg = file.readLines().firstOrNull { it.startsWith("package ") }
            ?.removePrefix("package ")?.trim() ?: return null
        return shorten(pkg)
    }

    private fun targetLayerOf(importPath: String): String? {
        if (importPath.startsWith("com.rainccup.aktool.resources")) return "resources"
        return shorten(importPath)
    }

    private fun shorten(fqn: String): String {
        val rest = fqn.removePrefix("com.rainccup.aktool").removePrefix(".")
        if (rest.isEmpty()) return "root"
        val parts = rest.split('.')
        return when (parts.first()) {
            "core", "feature", "ui" -> if (parts.size >= 2) "${parts[0]}.${parts[1]}" else parts[0]
            else -> parts.first()
        }
    }

    private fun findRepoRoot(): File? {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            if (File(dir, "settings.gradle.kts").isFile) return dir
            dir = dir.parentFile
        }
        return null
    }

    private companion object {
        /**
         * 目标态依赖表（NiA 分层）：
         * - feature → 只能依赖 domain / designsystem / model / navigation / common（数据来源必须经 use case）
         * - core.domain → data / model / common
         * - core.data → network / model / common / platform
         * - core.designsystem → model / common
         * - core.navigation → model
         * - core.network / core.message / core.platform → model / common（message 另需 platform 以适配 Messenger 端口）
         * - app → 全部（应用外壳聚合各层）
         * `resources`（Compose 资源）对任何层开放，见 isAllowed。
         *
         * 三处**有意保留**的例外（均为实测后确认，不是遗漏）：
         * 1. feature → core.platform：UI 直接使用平台端口（platformUiScale / urlEncode / Messenger /
         *    ClipboardPort 是 expect/actual 声明，UI 必须直接用；core.platform 因此视为「UI 可用的端口层」）。
         * 2. feature.characterdetail → feature.character：详情页复用干员页的卡片与画笔，并持有
         *    CharacterViewModel 以便保存后刷新列表；改成回调/事件总线是后续工作。
         * 3. core.common ↔ core.model 互引：core.common 的 LocalAppConfig 持有 AppConfig 模型，
         *    反向那条仅来自测试（ModelSerializationTest 用 JsonUtil 验证序列化）。
         */
        val allowedEdges: Set<Pair<String, String>> = setOf(
            "feature.character" to "core.common",
            "feature.character" to "core.designsystem",
            "feature.character" to "core.domain",
            "feature.character" to "core.model",
            "feature.character" to "core.navigation",
            "feature.characterdetail" to "core.common",
            "feature.characterdetail" to "core.designsystem",
            "feature.characterdetail" to "core.domain",
            "feature.characterdetail" to "core.model",
            "feature.characterdetail" to "core.navigation",
            "feature.extra" to "core.common",
            "feature.extra" to "core.designsystem",
            "feature.extra" to "core.domain",
            "feature.extra" to "core.model",
            "feature.extra" to "core.navigation",
            "feature.home" to "core.common",
            "feature.home" to "core.designsystem",
            "feature.home" to "core.domain",
            "feature.home" to "core.model",
            "feature.home" to "core.navigation",
            "feature.setting" to "core.common",
            "feature.setting" to "core.designsystem",
            "feature.setting" to "core.domain",
            "feature.setting" to "core.model",
            "feature.setting" to "core.navigation",
            "feature.splash" to "core.common",
            "feature.splash" to "core.designsystem",
            "feature.splash" to "core.domain",
            "feature.splash" to "core.model",
            "feature.splash" to "core.navigation",

            // 例外 1：UI 直接使用平台端口（expect/actual），见类注释
            "feature.character" to "core.platform",
            "feature.characterdetail" to "core.platform",
            "feature.extra" to "core.platform",
            "feature.home" to "core.platform",
            "feature.setting" to "core.platform",
            "feature.splash" to "core.platform",

            // 例外 2：详情页复用干员页的卡片/画笔与 CharacterViewModel
            "feature.characterdetail" to "feature.character",

            "core.domain" to "core.common",
            "core.domain" to "core.data",
            "core.domain" to "core.model",

            // 例外 3：core 内部互引，见类注释
            "core.common" to "core.model",
            "core.model" to "core.common",

            "core.data" to "core.common",
            "core.data" to "core.model",
            "core.data" to "core.network",
            "core.data" to "core.platform",

            "core.designsystem" to "core.common",
            "core.designsystem" to "core.model",

            "core.navigation" to "core.model",

            "core.network" to "core.common",
            "core.network" to "core.model",
            "core.message" to "core.common",
            "core.message" to "core.model",
            "core.message" to "core.platform",
            "core.platform" to "core.common",
            "core.platform" to "core.model",

            "app" to "core.common",
            "app" to "core.data",
            "app" to "core.designsystem",
            "app" to "core.domain",
            "app" to "core.message",
            "app" to "core.model",
            "app" to "core.navigation",
            "app" to "core.network",
            "app" to "core.platform",
            "app" to "feature.character",
            "app" to "feature.characterdetail",
            "app" to "feature.extra",
            "app" to "feature.home",
            "app" to "feature.setting",
            "app" to "feature.splash",
        )
    }
}
