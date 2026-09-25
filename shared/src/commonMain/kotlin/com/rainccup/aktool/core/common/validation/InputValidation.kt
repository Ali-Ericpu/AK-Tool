package com.rainccup.aktool.core.common.validation

/**
 * 主页数值字段的输入校验（原先是 `HomePage` 里的私有扩展函数）。
 * 允许空串或非负整数；其余一律视为非法。
 */
fun String.checkIntRange(): Boolean {
    val int = toIntOrNull()
    return int == null || int !in 0..Int.MAX_VALUE
}
