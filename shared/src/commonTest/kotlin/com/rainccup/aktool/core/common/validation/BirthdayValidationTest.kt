package com.rainccup.aktool.core.common.validation

import com.rainccup.aktool.core.model.Birthday
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `Birthday` 只有月/日，没有年份，所以 2 月按最多 29 天处理（闰年无从判断）。
 */
class BirthdayValidationTest {

    @Test
    fun everyMonthAcceptsItsOwnRange() {
        for (month in 1..12) {
            assertTrue(validBirthday(month, 1), "month $month day 1 should be valid")
            assertTrue(validBirthday(month, daysInMonth(month)), "month $month last day should be valid")
            assertFalse(validBirthday(month, daysInMonth(month) + 1), "month $month over its last day")
        }
        assertEquals(29, daysInMonth(2))
        assertEquals(30, daysInMonth(4))
        assertEquals(30, daysInMonth(6))
        assertEquals(30, daysInMonth(9))
        assertEquals(30, daysInMonth(11))
        assertEquals(31, daysInMonth(1))
        assertEquals(31, daysInMonth(12))
    }

    @Test
    fun rejectsMonthsAndDaysOutsideTheCalendar() {
        assertFalse(validBirthday(0, 1))
        assertFalse(validBirthday(13, 1))
        assertFalse(validBirthday(12, 32))
        assertFalse(validBirthday(1, 0))
        assertFalse(validBirthday(-5, -5))
        // 月份本身非法时天数上限为 0，调用方据此判定整条数据不可用
        assertEquals(0, daysInMonth(13))
        assertEquals(0, daysInMonth(0))
    }

    @Test
    fun feb30IsRejectedButFeb29Passes() {
        assertFalse(validBirthday(2, 30))
        assertTrue(validBirthday(2, 29))
    }

    @Test
    fun coercesDirtyServerValuesIntoTheMonth() {
        assertEquals(Birthday(2, 29), Birthday(2, 30).coerceToValid())
        assertEquals(Birthday(4, 1), Birthday(4, 0).coerceToValid())
        assertEquals(Birthday(1, 31), Birthday(1, 40).coerceToValid())
        // 月份非法时没有可信的月份可夹，退回 1 月 1 日
        assertEquals(Birthday(1, 1), Birthday(13, 45).coerceToValid())
        assertEquals(Birthday(12, 25), Birthday(12, 25).coerceToValid())
        // 夹完必须落在合法区间内
        listOf(Birthday(2, 31), Birthday(0, 99), Birthday(7, -3)).forEach { dirty ->
            val fixed = dirty.coerceToValid()
            assertTrue(validBirthday(fixed.month, fixed.day), "$dirty -> $fixed should be valid")
        }
    }

    @Test
    fun displaysPaddedAndNormalizedValues() {
        assertEquals("03-21", Birthday(3, 21).displayText())
        assertEquals("12-05", Birthday(12, 5).displayText())
        // 脏数据在展示前就被夹好，与选择器的初值保持一致
        assertEquals("02-29", Birthday(2, 30).displayText())
        assertEquals("01-01", Birthday(13, 45).displayText())
    }
}
