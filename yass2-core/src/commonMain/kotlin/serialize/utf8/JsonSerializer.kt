package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val LBRACE = '{'.code.toByte()
private const val RBRACE = '}'.code.toByte()

public class JsonSerializer(
    encoders: List<Utf8Encoder<*>>, multilineWrite: Boolean,
) : Utf8Serializer(encoders, multilineWrite, true) {

    private inner class TheWriter(writer: Writer) : Utf8Writer(writer) {
        private fun writeQuoted(string: String) {
            writeByte(QUOTE)
            writeString(string)
            writeByte(QUOTE)
        }

        private fun writeKey(key: String, expanded: Boolean = true) {
            if (expanded) writeIndent()
            writeQuoted(key)
            writeByte(COLON)
            if (expanded && multilineWrite) writeByte(SP)
        }

        private fun Utf8Encoder<*>.writeQuoted(value: Any?) {
            writeByte(QUOTE)
            write(this@TheWriter, value)
            writeByte(QUOTE)
        }

        override fun writeObject(value: Any?) {
            when (value) {
                null -> {
                    writeByte(LBRACE)
                    writeByte(RBRACE)
                }
                is String -> stringEncoder.write(this, value)
                is List<*> -> listEncoder.write(this, value)
                else -> {
                    writeByte(LBRACE)
                    val encoder = encoder(value::class)
                    val name = encoder.type.simpleName!!
                    if (encoder is ClassUtf8Encoder) {
                        nested {
                            writeNewLine()
                            writeKey("#")
                            writeQuoted(name)
                            encoder.write(this, value)
                            writeNewLine()
                        }
                        writeIndent()
                    } else {
                        writeKey("#$name", false)
                        encoder.writeQuoted(value)
                    }
                    writeByte(RBRACE)
                }
            }
        }

        private fun writeProperty(name: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            writeByte(COMMA)
            writeNewLine()
            writeKey(name)
            writeValue()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name, value) { writeObject(value) }
        }

        override fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name, value) {
                when (encoderId) {
                    STRING_ENCODER_ID -> stringEncoder.write(this, value)
                    LIST_ENCODER_ID -> listEncoder.write(this, value)
                    else -> encoder(encoderId).writeQuoted(value)
                }
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : Utf8Reader(reader, nextCodePoint) {
        override fun readString() = buildString {
            while (!expectedCodePoint(QUOTE)) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
        }

        private fun readKey(): String {
            check(expectedCodePoint(QUOTE)) { "'${QUOTE.toInt().toChar()}' expected" }
            readNextCodePoint()
            val key = readString()
            readNextCodePointAndSkipWhitespace()
            check(expectedCodePoint(COLON)) { "'${COLON.toInt().toChar()}' expected" }
            return key
        }

        override fun readObject(): Any? {
            skipWhitespace()
            return when {
                expectedCodePoint(QUOTE) -> stringEncoder.read(this)
                expectedCodePoint(LBRACKET) -> listEncoder.read(this)
                expectedCodePoint(LBRACE) -> {
                    readNextCodePointAndSkipWhitespace()
                    if (expectedCodePoint(RBRACE)) return null
                    val type = readKey()
                    readNextCodePointAndSkipWhitespace()
                    check(expectedCodePoint(QUOTE)) { "'${QUOTE.toInt().toChar()}' expected" }
                    readNextCodePoint()
                    check(type.isNotEmpty()) { "empty type" }
                    check(type[0] == '#') { "'#' expected" }
                    if (type.length > 1) {
                        val className = type.substring(1)
                        val encoder = encoder(className)
                        check(encoder !is ClassUtf8Encoder) { "is ClassUtf8Encoder" }
                        val value = encoder.read(this)
                        check(expectedCodePoint(QUOTE)) { "'${QUOTE.toInt().toChar()}' expected" }
                        readNextCodePointAndSkipWhitespace()
                        check(expectedCodePoint(RBRACE)) { "'${RBRACE.toInt().toChar()}' expected" }
                        value
                    } else {
                        val className = readString()
                        readNextCodePointAndSkipWhitespace()
                        readClass(encoder(className) as ClassUtf8Encoder)
                    }
                }
                else -> error("unexpected codePoint $nextCodePoint")
            }
        }

        private fun readClass(encoder: ClassUtf8Encoder<*>): Any {
            properties = mutableMapOf()
            while (!expectedCodePoint(RBRACE)) {
                check(expectedCodePoint(COMMA)) { "'${COMMA.toInt().toChar()}' expected" }
                readNextCodePointAndSkipWhitespace()
                val name = readKey()
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val value = if (encoderId == NO_ENCODER_ID) with(TheReader(this, nextCodePoint)) { readObject() } else {
                    check(expectedCodePoint(QUOTE)) { "'${QUOTE.toInt().toChar()}' expected" }
                    readNextCodePoint()
                    encoder(encoderId).read(this)
                }
                readNextCodePointAndSkipWhitespace()
                encoder.addProperty(name, value)
            }
            return encoder.read(this)
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer).writeObject(value)
    override fun read(reader: Reader): Any? = TheReader(reader, reader.readCodePoint()).readObject()
}
