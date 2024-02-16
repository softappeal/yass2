package ch.softappeal.yass2

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame
import kotlin.test.assertTrue

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
    fun withSuspend() = runTest {
        var tryCalled = false
        var finallyCalled = false

        @Suppress("RedundantSuspendModifier")
        suspend fun tryBlock(): Int {
            tryCalled = true
            return 123
        }

        @Suppress("RedundantSuspendModifier")
        suspend fun finallyBlock(): Int {
            finallyCalled = true
            return 321
        }

        assertEquals(
            123,
            tryFinally({
                tryBlock()
            }) {
                finallyBlock()
            }
        )
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
    }
}
