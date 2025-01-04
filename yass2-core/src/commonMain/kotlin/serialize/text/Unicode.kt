package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.serialize.Reader

// see https://docs.oracle.com/javase/tutorial/i18n/text/unicode.html

// see https://en.wikipedia.org/wiki/Universal_Character_Set_characters#Surrogates
internal fun StringBuilder.addCodePoint(codePoint: Int) {
    if (codePoint in 0 until 0x10000) append(codePoint.toChar()) else {
        val cp = codePoint - 0x10000
        append(((cp ushr 10) + 0xD800).toChar()) // high surrogate
        append(((cp and 0x3FF) + 0xDC00).toChar()) // low surrogate
    }
}

// see https://en.wikipedia.org/wiki/UTF-8#Description
internal fun Reader.readCodePoint(): Int {
    fun rb() = readByte().toInt()
    val first = rb()
    return when {
        first and 0b1000_0000 == 0b0000_0000 ->
            first
        first and 0b1110_0000 == 0b1100_0000 ->
            ((first and 0b0001_1111) shl 6) or
                (rb() and 0b0011_1111)
        first and 0b1111_0000 == 0b1110_0000 ->
            ((first and 0b000_1111) shl 12) or
                ((rb() and 0b0011_1111) shl 6) or
                (rb() and 0b0011_1111)
        else ->
            ((first and 0b0000_0111) shl 18) or
                ((rb() and 0b0011_1111) shl 12) or
                ((rb() and 0b0011_1111) shl 6) or
                (rb() and 0b0011_1111)
    }
}
