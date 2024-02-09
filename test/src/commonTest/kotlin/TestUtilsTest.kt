package ch.softappeal.yass2

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUtilsTest {
    @Test
    fun assertSuspendFailsWith() = runTest {
        assertEquals(
            "hello",
            assertSuspendFailsWith<RuntimeException> { throw NullPointerException("hello") }.message
        )
        assertSuspendFailsWith<RuntimeException> { throw RuntimeException() }
        try {
            assertSuspendFailsWith<RuntimeException> { throw Exception() }
        } catch (ignore: AssertionError) {
        }
        try {
            assertSuspendFailsWith<RuntimeException> { }
        } catch (ignore: AssertionError) {
        }
        assertSuspendFailsWith<AssertionError> { throw AssertionError() }
        assertSuspendFailsWith<Throwable> { throw AssertionError() }
    }
}
