package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.*

fun Writer.writeBoolean(value: Boolean) {
    writeByte(if (value) 1 else 0)
}

fun Reader.readBoolean(): Boolean = readByte().toInt() != 0

fun Writer.writeInt(value: Int) {
    writeByte((value shr 24).toByte())
    writeByte((value shr 16).toByte())
    writeByte((value shr 8).toByte())
    writeByte(value.toByte())
}

fun Reader.readInt(): Int =
    (readByte().toInt() and 0xFF shl 24) or
        (readByte().toInt() and 0xFF shl 16) or
        (readByte().toInt() and 0xFF shl 8) or
        (readByte().toInt() and 0xFF)

fun Writer.writeLong(value: Long) {
    writeByte((value shr 56).toByte())
    writeByte((value shr 48).toByte())
    writeByte((value shr 40).toByte())
    writeByte((value shr 32).toByte())
    writeByte((value shr 24).toByte())
    writeByte((value shr 16).toByte())
    writeByte((value shr 8).toByte())
    writeByte(value.toByte())
}

fun Reader.readLong(): Long =
    (readByte().toLong() and 0xFF shl 56) or
        (readByte().toLong() and 0xFF shl 48) or
        (readByte().toLong() and 0xFF shl 40) or
        (readByte().toLong() and 0xFF shl 32) or
        (readByte().toLong() and 0xFF shl 24) or
        (readByte().toLong() and 0xFF shl 16) or
        (readByte().toLong() and 0xFF shl 8) or
        (readByte().toLong() and 0xFF)
