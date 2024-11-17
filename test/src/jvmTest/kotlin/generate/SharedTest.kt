package ch.softappeal.yass2.generate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedTest {
    @Test
    fun hasNoDuplicates() {
        assertTrue(listOf(1, 2).hasNoDuplicates())
        assertFalse(listOf(1, 1).hasNoDuplicates())
    }

    @Test
    fun duplicates() {
        assertTrue(listOf(1, 2).duplicates().isEmpty())
        assertEquals(listOf(1, 3, 3), listOf(3, 1, 2, 1, 3, 3).duplicates())
        assertEquals(listOf(1, 3, 3), listOf(1, 3, 2, 1, 3, 3).duplicates())
        assertEquals(listOf(3, 1, 3), listOf(1, 3, 2, 3, 1, 3).duplicates())
    }
}
