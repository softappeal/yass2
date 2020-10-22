package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.*

fun String.utf8Length(): Int {
    var bytes = 0
    var c = 0
    while (c < length) {
        val ch = get(c)
        val codePoint = ch.toInt()
        bytes += when {
            codePoint < 0x80 -> 1
            codePoint < 0x800 -> 2
            ch.isHighSurrogate() -> {
                c++
                4
            }
            else -> 3
        }
        c++
    }
    return bytes
}

@Suppress("SpellCheckingInspection")
fun Writer.toUtf8(value: String) {
    var c = 0
    while (c < value.length) {
        val ch = value[c]
        val codePoint = ch.toInt()
        when {
            codePoint < 0x80 -> { // 0xxx_xxxx
                writeByte(codePoint.toByte())
            }
            codePoint < 0x800 -> { // 110x_xxxx 10xx_xxxx
                writeByte((codePoint ushr 6 and 0x1F or 0xC0).toByte())
                writeByte((codePoint and 0x3F or 0x80).toByte())
            }
            ch.isHighSurrogate() -> { // 1111_0xxx 10xx_xxxx 10xx_xxxx 10xx_xxxx
                val highSurrogate = codePoint - 0xD800
                val lowSurrogate = value[++c].toInt() - 0xDC00
                val codePoint2 = (highSurrogate shl 10) + lowSurrogate + 0x10000
                writeByte((codePoint2 ushr 18 and 0x07 or 0xF0).toByte())
                writeByte((codePoint2 ushr 12 and 0x3F or 0x80).toByte())
                writeByte((codePoint2 ushr 6 and 0x3F or 0x80).toByte())
                writeByte((codePoint2 and 0x3F or 0x80).toByte())
            }
            else -> { // 1110_xxxx 10xx_xxxx 10xx_xxxx
                writeByte((codePoint ushr 12 and 0x0F or 0xE0).toByte())
                writeByte((codePoint ushr 6 and 0x3F or 0x80).toByte())
                writeByte((codePoint and 0x3F or 0x80).toByte())
            }
        }
        c++
    }
}

@Suppress("SpellCheckingInspection")
fun Reader.fromUtf8(utf8Length: Int): String {
    val chars = CharArray(utf8Length) // could be too big but that's ok
    var c = 0
    var l = utf8Length
    while (l-- > 0) {
        var codePoint: Int
        val b1 = readByte().toInt()
        if (b1 and 0x80 == 0) { // 0xxx_xxxx
            codePoint = b1
        } else {
            val b2 = readByte().toInt()
            l--
            if (b1 and 0xE0 == 0xC0) { // 110x_xxxx 10xx_xxxx
                codePoint = (b1 and 0x1F shl 6) or (b2 and 0x3F)
            } else {
                val b3 = readByte().toInt()
                l--
                if (b1 and 0xF0 == 0xE0) { // 1110_xxxx 10xx_xxxx 10xx_xxxx
                    codePoint = (b1 and 0x0F shl 12) or (b2 and 0x3F shl 6) or (b3 and 0x3F)
                } else { // 1111_0xxx 10xx_xxxx 10xx_xxxx 10xx_xxxx
                    val b4 = readByte().toInt()
                    l--
                    val cp = ((b1 and 0x07 shl 18) or (b2 and 0x3F shl 12) or (b3 and 0x3F shl 6) or (b4 and 0x3F)) - 0x10000
                    chars[c++] = ((cp ushr 10) + 0xD800).toChar() // highSurrogate
                    codePoint = (cp and 0x3FF) + 0xDC00 // lowSurrogate
                }
            }
        }
        chars[c++] = codePoint.toChar()
    }
    return chars.concatToString(0, c)
}
