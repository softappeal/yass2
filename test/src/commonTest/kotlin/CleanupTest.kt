package ch.softappeal.yass2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Suppress("ASSIGNED_VALUE_IS_NEVER_READ")
class CleanupTest {
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

    @Test
    fun noTryExceptionNoCatchException() {
        var tryCalled = false
        var catchCalled = false
        assertEquals(
            123,
            tryCatch({
                tryCalled = true
                123
            }) {
                catchCalled = true
            }
        )
        assertTrue(tryCalled)
        assertFalse(catchCalled)
    }

    @Test
    fun withTryExceptionNoCatchException() {
        var tryCalled = false
        val tryException = Exception()
        var catchCalled = false
        assertSame(
            tryException,
            assertFails {
                tryCatch({
                    tryCalled = true
                    throw tryException
                }) {
                    catchCalled = true
                }
            }
        )
        assertTrue(tryCalled)
        assertTrue(catchCalled)
    }

    @Test
    fun withTryExceptionWithCatchException() {
        var tryCalled = false
        val tryException = Exception()
        var catchCalled = false
        val catchException = Exception()
        assertSame(
            tryException,
            assertFails {
                tryCatch({
                    tryCalled = true
                    throw tryException
                }) {
                    catchCalled = true
                    throw catchException
                }
            }
        )
        assertTrue(tryCalled)
        assertTrue(catchCalled)
        assertEquals(listOf(catchException), tryException.suppressedExceptions)
    }
}
