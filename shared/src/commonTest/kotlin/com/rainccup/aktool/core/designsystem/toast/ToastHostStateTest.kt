package com.rainccup.aktool.core.designsystem.toast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ToastHostStateTest {

    @Test
    fun latestMessageReplacesPrevious() {
        val state = ToastHostState()

        state.show("first")
        val first = state.current
        state.show("second")

        assertEquals("second", state.current?.text)
        assertNotEquals(first, state.current)
    }

    /** 旧消息的定时器晚到时，不能把已经顶替它的新消息一起清掉。 */
    @Test
    fun staleDismissKeepsNewerMessage() {
        val state = ToastHostState()
        state.show("first")
        val first = state.current!!
        state.show("second")

        state.dismiss(first)

        assertEquals("second", state.current?.text)
    }

    @Test
    fun dismissClearsCurrent() {
        val state = ToastHostState()
        state.show("only")

        state.dismiss(state.current!!)

        assertNull(state.current)
    }

    @Test
    fun durationFollowsTheMessage() {
        val state = ToastHostState()

        state.show("slow", durationMillis = 5_000L)
        assertEquals(5_000L, state.current?.durationMillis)

        state.show("default")
        assertEquals(ToastMessage.LENGTH_SHORT, state.current?.durationMillis)
    }
}
