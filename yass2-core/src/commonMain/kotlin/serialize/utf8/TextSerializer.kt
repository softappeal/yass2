package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val LPAREN = '('.code.toByte()
private const val RPAREN = ')'.code.toByte()
private const val ASTERIX = '*'.code.toByte() // null

public class TextSerializer(
    encoders: List<Utf8Encoder<*>>, multilineWrite: Boolean,
) : Utf8Serializer(encoders, multilineWrite, false) {

    private inner class TheWriter(writer: Writer) : Utf8Writer(writer) {
        override fun writeObject(value: Any?) {
            when (value) {
                null -> writeByte(ASTERIX)
                is String -> stringEncoder.write(this, value)
                is List<*> -> listEncoder.write(this, value)
                else -> {
                    val encoder = encoder(value::class)
                    writeString(encoder.type.simpleName!!)
                    writeByte(LPAREN)
                    if (encoder !is ClassUtf8Encoder) encoder.write(this, value) else {
                        writeNewLine()
                        nested { encoder.write(this, value) }
                        writeIndent()
                    }
                    writeByte(RPAREN)
                }
            }
        }

        private var firstProperty: Boolean = true

        private fun writeProperty(name: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            if (multilineWrite || firstProperty) firstProperty = false else writeByte(COMMA)
            writeIndent()
            writeString(name)
            writeByte(COLON)
            if (multilineWrite) writeByte(SP)
            writeValue()
            writeNewLine()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name, value) { TheWriter(this).writeObject(value) }
        }

        override fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name, value) { encoder(encoderId).write(this, value) }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : Utf8Reader(reader, nextCodePoint) {
        private fun readUntil(isEnd: () -> Boolean): String = buildString {
            while (!isEnd() && !isWhitespace()) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
            skipWhitespace()
        }

        private fun readUntil(end: Byte) = readUntil { expectedCodePoint(end) }

        override fun readString() = readUntil { expectedCodePoint(RPAREN) || expectedCodePoint(COMMA) }

        override fun readObject(): Any? {
            skipWhitespace()
            return when {
                expectedCodePoint(ASTERIX) -> null
                expectedCodePoint(QUOTE) -> stringEncoder.read(this)
                expectedCodePoint(LBRACKET) -> listEncoder.read(this)
                else -> {
                    val className = readUntil(LPAREN)
                    readNextCodePointAndSkipWhitespace()
                    val encoder = encoder(className)
                    if (encoder is ClassUtf8Encoder) readClass(encoder) else encoder.read(this)
                }
            }
        }

        private fun readClass(encoder: ClassUtf8Encoder<*>): Any {
            properties = mutableMapOf()
            while (!expectedCodePoint(RPAREN)) {
                val name = readUntil(COLON)
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val value = if (encoderId != NO_ENCODER_ID) encoder(encoderId).read(this) else {
                    with(TheReader(this, nextCodePoint)) { readObject() }.apply { readNextCodePoint() }
                }
                skipWhitespace()
                encoder.addProperty(name, value)
                if (expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
            }
            return encoder.read(this)
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer).writeObject(value)
    override fun read(reader: Reader): Any? = TheReader(reader, reader.readCodePoint()).readObject()
}
