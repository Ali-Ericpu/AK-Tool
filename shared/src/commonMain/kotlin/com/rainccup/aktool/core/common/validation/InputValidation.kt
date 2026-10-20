package com.rainccup.aktool.core.common.validation

import com.rainccup.aktool.core.model.Birthday

/**
 * 主页数值字段的输入校验（原先是 `HomePage` 里的私有扩展函数）。
 * 允许空串或非负整数；其余一律视为非法。
 */
fun String.checkIntRange(): Boolean {
    val int = toIntOrNull()
    return int == null || int !in 0..Int.MAX_VALUE
}

/**
 * 生日的不含年份的合法性判定：`Birthday` 只有 month/day，没有年份，
 * 所以闰年无从判断 —— 2 月一律放宽到 29 天（2/29 视为合法生日）。
 */
fun validBirthday(month: Int, day: Int): Boolean =
    month in MONTH_MIN..MONTH_MAX && day in DAY_MIN..daysInMonth(month)

/** 该月份可容纳的最大天数；月份本身非法时返回 0，调用方据此可知整条数据不可用。 */
fun daysInMonth(month: Int): Int = when (month) {
    in MONTH_MIN..MONTH_MAX -> when (month) {
        2 -> 29
        4, 6, 9, 11 -> 30
        else -> 31
    }

    else -> 0
}

/** 服务端可能返回 2/30 之类的脏数据，夹到该月最后一天，避免选择器拿到越界初值。 */
fun Birthday.coerceToValid(): Birthday =
    if (month !in MONTH_MIN..MONTH_MAX) Birthday(MONTH_MIN, DAY_MIN)
    else Birthday(month, day.coerceIn(DAY_MIN, daysInMonth(month)))

/**
 * 展示用的 `MM-DD`。先夹到合法范围再格式化，使条目显示与选择器打开时的初值始终一致，
 * 不会出现「列表写 2-30、弹窗却是 2-29」这种自相矛盾的画面。
 */
fun Birthday.displayText(): String {
    val fixed = coerceToValid()
    return fixed.month.toString().padStart(2, '0') + "-" + fixed.day.toString().padStart(2, '0')
}

private const val MONTH_MIN = 1
private const val MONTH_MAX = 12
private const val DAY_MIN = 1
