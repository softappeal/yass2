package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.transport.*
import kotlin.test.*

class Utf8Test {
    @Suppress("SpellCheckingInspection")
    @Test
    fun test() {
        fun check(s: String, vararg bytes: Int) {
            val buffer = ByteArray(s.utf8Length())
            with(BytesWriter(buffer)) {
                toUtf8(s)
                assertEquals(buffer.size, current)
                assertEquals(current, bytes.size)
                checkTail(*bytes)
            }
            with(BytesReader(buffer)) {
                val s2 = fromUtf8(bytes.size)
                assertTrue(drained)
                assertEquals(s, s2)
            }
        }
        check("")
        check("abc", 97, 98, 99)
        check("\u0000\u0001\u007F", 0x00, 0x01, 0x7F)
        check("\u0080", -62, 0x80)
        check("\u07FF", -33, -65)
        check("\u0800", -32, -96, 0x80)
        check("\uFFFF", -17, -65, -65)
        check("\uD800\uDC00", -16, -112, -128, -128) // U+010000
        check("\uD800\uDC01", -16, -112, -128, -127) // U+010001
        check("\uDBFF\uDFFF", -12, -113, -65, -65)   // U+1FFFFF
    }
}
