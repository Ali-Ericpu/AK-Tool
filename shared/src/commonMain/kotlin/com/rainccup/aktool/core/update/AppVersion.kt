package com.rainccup.aktool.core.update

/**
 * 够用就好的语义化版本号：只比较数字段与预发布后缀。
 *
 * 支持 `2.0.5`、`v2.0.5`、`2.0.5-rc1`、`2.0.5+20261024` 这些真实可能出现在 tag 上的写法。
 */
data class AppVersion(
    private val numbers: List<Int>,
    private val preRelease: String? = null,
) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int {
        val segments = maxOf(numbers.size, other.numbers.size)
        for (index in 0 until segments) {
            val left = numbers.getOrElse(index) { 0 }
            val right = other.numbers.getOrElse(index) { 0 }
            if (left != right) return left.compareTo(right)
        }
        return when {
            preRelease == null && other.preRelease == null -> 0
            // 同一数字段下：正式版 > 预发布版
            preRelease == null -> 1
            other.preRelease == null -> -1
            else -> preRelease.compareTo(other.preRelease)
        }
    }

    override fun toString(): String =
        numbers.joinToString(".") + (preRelease?.let { "-$it" } ?: "")

    companion object {
        private val Pattern = Regex("""^[vV]?(\d+(?:\.\d+)*)(?:-([0-9A-Za-z.-]+))?(?:\+.*)?$""")

        fun parse(raw: String): AppVersion? {
            val match = Pattern.find(raw.trim()) ?: return null
            val numbers = match.groupValues[1]
                .split('.')
                .map { it.toIntOrNull() ?: return null }
            if (numbers.isEmpty()) return null
            val pre = match.groupValues[2].takeIf { it.isNotEmpty() }
            return AppVersion(numbers, pre)
        }
    }
}
