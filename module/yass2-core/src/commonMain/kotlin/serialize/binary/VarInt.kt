package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.*

// see https://developers.google.com/protocol-buffers/docs/encoding#varints

private const val IntInv7F: Int = 0x7F.inv()

public fun Writer.writeVarInt(value: Int) {
    var v = value
    while (true) {
        if (v and IntInv7F == 0) {
            writeByte(v.toByte())
            return
        }
        writeByte((v and 0x7F or 0x80).toByte())
        v = v ushr 7
    }
}

public fun Reader.readVarInt(): Int {
    var shift = 0
    var value = 0
    while (true) {
        val b = readByte().toInt()
        value = value or (b and 0x7F shl shift)
        if (b and 0x80 == 0) return value
        shift += 7
    }
}

private const val LongInv7F: Long = 0x7FL.inv()

public fun Writer.writeVarLong(value: Long) {
    var v = value
    while (true) {
        if (v and LongInv7F == 0L) {
            writeByte(v.toByte())
            return
        }
        writeByte((v and 0x7F or 0x80).toByte())
        v = v ushr 7
    }
}

public fun Reader.readVarLong(): Long {
    var shift = 0
    var value = 0L
    while (true) {
        val b = readByte().toLong()
        value = value or (b and 0x7F shl shift)
        if (b and 0x80 == 0L) return value
        shift += 7
    }
}

// see https://developers.google.com/protocol-buffers/docs/encoding#signed_integers

public fun Int.toZigZag(): Int = (this shl 1) xor (this shr 31)

public fun Int.fromZigZag(): Int = (this ushr 1) xor -(this and 1)

public fun Long.toZigZag(): Long = (this shl 1) xor (this shr 63)

public fun Long.fromZigZag(): Long = (this ushr 1) xor -(this and 1)
