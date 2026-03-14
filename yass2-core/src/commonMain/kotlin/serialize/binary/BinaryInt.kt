package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer

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
