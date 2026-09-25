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
        from == to || allowedEdges.any { it.first == from && it.second == to }

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
         * 允许的包间依赖边。
         * 迁移期间 = 迁移前的现状（69 条，由 printCurrentEdges 生成）；T5 收紧到 NiA 目标态。
         */
        val allowedEdges: Set<Pair<String, String>> = setOf(
            "core.common" to "core.model",
            "core.data" to "core.common",
            "core.designsystem" to "core.model",
            "core.designsystem" to "resources",
            "core.data" to "core.model",
            "core.data" to "core.network",
            "core.data" to "core.platform",
            "core.data" to "core.data",
            "core.message" to "core.platform",
            "core.model" to "core.common",
            "core.model" to "resources",
            "core.network" to "core.common",
            "core.network" to "core.model",
            "core.data" to "core.model",
            "core.data" to "core.network",
            "di" to "core.data",
            "di" to "core.message",
            "di" to "core.network",
            "di" to "core.platform",
            "di" to "core.data",
            "di" to "ui.character",
            "di" to "ui.characterdetail",
            "di" to "ui.extra",
            "di" to "ui.home",
            "di" to "ui.setting",
            "di" to "ui.splash",
            "root" to "core.common",
            "root" to "core.designsystem",
            "root" to "core.data",
            "root" to "core.model",
            "root" to "core.network",
            "root" to "ui.navigation",
            "root" to "ui.splash",
            "root" to "ui.theme",
            "ui.character" to "core.data",
            "ui.character" to "core.designsystem",
            "ui.characterdetail" to "core.designsystem",
            "ui.extra" to "core.designsystem",
            "ui.home" to "core.designsystem",
            "ui.setting" to "core.designsystem",
            "ui.splash" to "core.designsystem",
            "ui.character" to "core.model",
            "ui.character" to "core.platform",
            "ui.character" to "core.data",
            "ui.character" to "resources",
            "ui.character" to "ui.setting",
            "ui.character" to "core.common",
            "ui.characterdetail" to "core.data",
            "ui.characterdetail" to "core.model",
            "ui.characterdetail" to "core.platform",
            "ui.characterdetail" to "resources",
            "ui.characterdetail" to "ui.character",
            "ui.characterdetail" to "ui.setting",
            "ui.characterdetail" to "ui.splash",
            "ui.extra" to "core.data",
            "ui.extra" to "core.model",
            "ui.extra" to "core.platform",
            "ui.extra" to "core.data",
            "ui.extra" to "resources",
            "ui.extra" to "ui.characterdetail",
            "ui.extra" to "ui.setting",
            "ui.home" to "core.model",
            "ui.home" to "core.platform",
            "ui.home" to "core.data",
            "ui.home" to "resources",
            "ui.home" to "ui.setting",
            "ui.navigation" to "core.message",
            "ui.navigation" to "resources",
            "ui.navigation" to "ui.character",
            "ui.navigation" to "ui.characterdetail",
            "ui.navigation" to "ui.extra",
            "ui.navigation" to "ui.home",
            "ui.navigation" to "ui.setting",
            "ui.setting" to "core.common",
            "ui.setting" to "core.data",
            "ui.setting" to "core.network",
            "ui.setting" to "core.platform",
            "ui.setting" to "resources",
            "ui.splash" to "resources",
            "core.domain" to "core.data",
            "core.domain" to "core.model",
            "core.domain" to "core.network",
            "di" to "core.domain",
            "ui.character" to "core.domain",
            "ui.characterdetail" to "core.domain",
            "ui.extra" to "core.domain",
            "ui.home" to "core.common",
            "ui.home" to "core.domain",
            "ui.setting" to "core.domain",
        )
    }
}
