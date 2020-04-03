package ch.softappeal.yass2

import kotlin.test.*

class CleanupTest {
    @Test
    fun addSuppressedWithoutException() {
        val e = Exception()
        var called = false
        val e2 = e.addSuppressed { called = true }
        assertTrue(called)
        assertSame(e, e2)
    }

    @Test
    fun addSuppressedWithException() {
        val e = Exception()
        var called = false
        val blockException = Exception()
        val e2 = e.addSuppressed {
            called = true
            throw blockException
        }
        assertTrue(called)
        assertSame(e, e2)
        assertEquals(listOf(blockException), e.suppressedExceptions)
    }

    @Test
    fun addSuppressedWithSuspend() = yassRunBlocking {
        val e = Exception()
        var called = false

        @Suppress("RedundantSuspendModifier")
        suspend fun block() {
            called = true
        }

        val e2 = e.addSuppressed { block() }
        assertTrue(called)
        assertSame(e, e2)
    }

    @Test
    fun tryFinallyNoTryExceptionNoFinallyException() {
        var tryCalled = false
        var finallyCalled = false
        val r = tryFinally({
            tryCalled = true
            123
        }) {
            finallyCalled = true
        }
        assertEquals(123, r)
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
    }

    @Test
    fun tryFinallyWithTryExceptionNoFinallyException() {
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
    fun tryFinallyNoTryExceptionWithFinallyException() {
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
    fun tryFinallyWithTryExceptionWithFinallyException() {
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
    fun tryFinallyWithSuspend() = yassRunBlocking {
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

        val r = tryFinally({
            tryBlock()
        }) {
            finallyBlock()
        }
        assertEquals(123, r)
        assertTrue(tryCalled)
        assertTrue(finallyCalled)
    }
}
