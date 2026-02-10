package ch.softappeal.yass2.core.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class MessageTest {
    @Test
    fun valueReply() {
        val value = 123
        assertEquals(value, ValueReply(value).process())
    }

    @Test
    fun exceptionReply() {
        val exception = Exception()
        assertSame(
            exception,
            assertFailsWith { ExceptionReply(exception).process() },
        )
    }
}
