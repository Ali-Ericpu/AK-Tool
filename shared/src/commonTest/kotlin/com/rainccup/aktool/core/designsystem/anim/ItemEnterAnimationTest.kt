package com.rainccup.aktool.core.designsystem.anim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemEnterAnimationTest {

    @Test
    fun delayGrowsWithIndex() {
        assertEquals(0L, itemEnterDelayMillis(0))
        assertEquals(30L, itemEnterDelayMillis(1))
        assertEquals(90L, itemEnterDelayMillis(3))
    }

    /** 滚到很后面时不能越等越久：封顶之后所有条目同时入场。 */
    @Test
    fun delayIsCapped() {
        val cap = itemEnterDelayMillis(10)
        assertEquals(cap, itemEnterDelayMillis(11))
        assertEquals(cap, itemEnterDelayMillis(500))

        // 封顶值要够短，不然往下滚到第 20 项才出现，观感像卡顿
        assertTrue(cap <= 400L)
    }

    @Test
    fun negativeIndexFallsBackToZero() {
        assertEquals(0L, itemEnterDelayMillis(-1))
    }
}
