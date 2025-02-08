package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val FALSE: Byte = 0
private const val TRUE: Byte = 1

public fun Writer.writeBinaryBoolean(value: Boolean) {
    writeByte(if (value) TRUE else FALSE)
}

public fun Reader.readBinaryBoolean(): Boolean = when (val b = readByte()) {
    FALSE -> false
    TRUE -> true
    else -> error("unexpected binary boolean $b")
}

public fun Writer.writeBinaryInt(value: Int) {
    writeByte((value shr 24).toByte())
    writeByte((value shr 16).toByte())
    writeByte((value shr 8).toByte())
    writeByte(value.toByte())
}

public fun Reader.readBinaryInt(): Int =
    (readByte().toInt() and 0xFF shl 24) or
        (readByte().toInt() and 0xFF shl 16) or
        (readByte().toInt() and 0xFF shl 8) or
        (readByte().toInt() and 0xFF)

public fun Writer.writeBinaryLong(value: Long) {
    writeByte((value shr 56).toByte())
    writeByte((value shr 48).toByte())
    writeByte((value shr 40).toByte())
    writeByte((value shr 32).toByte())
    writeByte((value shr 24).toByte())
    writeByte((value shr 16).toByte())
    writeByte((value shr 8).toByte())
    writeByte(value.toByte())
}

public fun Reader.readBinaryLong(): Long =
    (readByte().toLong() and 0xFF shl 56) or
        (readByte().toLong() and 0xFF shl 48) or
        (readByte().toLong() and 0xFF shl 40) or
        (readByte().toLong() and 0xFF shl 32) or
        (readByte().toLong() and 0xFF shl 24) or
        (readByte().toLong() and 0xFF shl 16) or
        (readByte().toLong() and 0xFF shl 8) or
        (readByte().toLong() and 0xFF)
