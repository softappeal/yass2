package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.serialize.Reader

// see https://docs.oracle.com/javase/tutorial/i18n/text/unicode.html

// see https://en.wikipedia.org/wiki/Universal_Character_Set_characters#Surrogates
public fun StringBuilder.addCodePoint(codePoint: Int) {
    if (codePoint in 0..Char.MAX_VALUE.code) append(codePoint.toChar()) else {
        val cp = codePoint - (Char.MAX_VALUE.code + 1)
        append(((cp ushr 10) + Char.MIN_HIGH_SURROGATE.code).toChar())
        append(((cp and 0b11_1111_1111) + Char.MIN_LOW_SURROGATE.code).toChar())
    }
}

// see https://en.wikipedia.org/wiki/UTF-8#Description
public fun Reader.readCodePoint(): Int {
    val firstByte = readByte().toInt()
    fun followByte() = readByte().toInt().apply {
        check(this and 0b1100_0000 == 0b1000_0000) { "invalid follow byte ${toUByte()}" }
    }
    return when {
        firstByte and 0b1000_0000 == 0b0000_0000 -> firstByte
        firstByte and 0b1110_0000 == 0b1100_0000 -> (
            ((firstByte and 0b0001_1111) shl 6) or
                (followByte() and 0b0011_1111)
            ).apply {
                check(this >= 0x80) { "overlong 2 byte encoding of ${toUInt()}" }
            }
        firstByte and 0b1111_0000 == 0b1110_0000 -> (
            ((firstByte and 0b000_1111) shl 12) or
                ((followByte() and 0b0011_1111) shl 6) or
                (followByte() and 0b0011_1111)
            ).apply {
                check(this >= 0x800) { "overlong 3 byte encoding of ${toUInt()}" }
                check(this !in 0xD800..0xDFFF) { "illegal surrogate ${toUInt()}" }
            }
        firstByte and 0b1111_1000 == 0b1111_0000 -> (
            ((firstByte and 0b0000_0111) shl 18) or
                ((followByte() and 0b0011_1111) shl 12) or
                ((followByte() and 0b0011_1111) shl 6) or
                (followByte() and 0b0011_1111)
            ).apply {
                check(this >= 0x10000) { "overlong 4 byte encoding of ${toUInt()}" }
            }
        else -> error("invalid first byte ${firstByte.toUByte()}")
    }
}
