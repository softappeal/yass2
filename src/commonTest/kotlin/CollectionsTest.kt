package ch.softappeal.yass2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CollectionsTest {
    @Test
    fun forEachSeparator() {
        emptyList<Int>().forEachSeparator({ fail() }) { fail() }

        var s = ""
        listOf("a").forEachSeparator({ s += "-" }) { s += it }
        assertEquals("a", s)

        s = ""
        listOf("a", "b", "c").forEachSeparator({ s += "-" }) { s += it }
        assertEquals("a-b-c", s)
    }
}
