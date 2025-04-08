package ch.softappeal.yass2.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Suppress("ASSIGNED_VALUE_IS_NEVER_READ")
class CleanupTest {
    @Test
    fun addSuppressedNoException() {
        var called = false
        val exception = Exception()
        assertSame(
            exception,
            exception.addSuppressed { called = true },
        )
        assertTrue(called)
        assertEquals(listOf(), exception.suppressedExceptions)
    }

    @Test
    fun addSuppressedWithException() {
        val exception = Exception()
        val suppressed = Exception()
        assertSame(
            exception,
            exception.addSuppressed { throw suppressed },
        )
        assertEquals(listOf(suppressed), exception.suppressedExceptions)
    }

    @Test
    fun noTryExceptionNoFinallyException() {
        var tryCalled = false
        var finallyCalled = false
        assertEquals(
            123,
            tryFinally({
                tryCalled = true
                123
            }) {
                finallyCalled = true
            }
        )
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
    }

    @Test
    fun withTryExceptionNoFinallyException() {
        var tryCalled = false
        val tryException = Exception()
        var finallyCalled = false
        assertSame(
            tryException,
            assertFails {
                tryFinally({
                    tryCalled = true
                    throw tryException
                }) {
                    finallyCalled = true
                }
            }
        )
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
    }

    @Test
    fun noTryExceptionWithFinallyException() {
        var tryCalled = false
        var finallyCalled = false
        val finallyException = Exception()
        assertSame(
            finallyException,
            assertFails {
                tryFinally({
                    tryCalled = true
                }) {
                    finallyCalled = true
                    throw finallyException
                }
            }
        )
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
    }

    @Test
    fun withTryExceptionWithFinallyException() {
        var tryCalled = false
        val tryException = Exception()
        var finallyCalled = false
        val finallyException = Exception()
        assertSame(
            tryException,
            assertFails {
                tryFinally({
                    tryCalled = true
                    throw tryException
                }) {
                    finallyCalled = true
                    throw finallyException
                }
            }
        )
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
        assertEquals(listOf(finallyException), tryException.suppressedExceptions)
    }
}
