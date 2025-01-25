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

        override fun writeWithId(value: Any?) {
            when (value) {
                null -> {
                    writeByte(LBRACE)
                    writeKey("#", false)
                    writeQuoted("")
                    writeByte(RBRACE)
                }
                is String -> StringEncoder.write(this, value)
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

        private fun writeProperty(property: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            writeByte(COMMA)
            writeNewLine()
            writeKey(property)
            writeValue()
        }

        override fun writeWithId(property: String, value: Any?) {
            writeProperty(property, value) { writeWithId(value) }
        }

        override fun writeNoId(property: String, id: Int, value: Any?) {
            writeProperty(property, value) {
                when (id) {
                    STRING_ENCODER_ID -> StringEncoder.write(this, value)
                    LIST_ENCODER_ID -> listEncoder.write(this, value)
                    else -> encoder(id).writeQuoted(value)
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

        private fun skipQuote() {
            readNextCodePointAndSkipWhitespace()
            check(expectedCodePoint(QUOTE)) { "'${QUOTE.toInt().toChar()}' expected" }
            readNextCodePoint()
        }

        private fun readKey(): String {
            skipQuote()
            val key = readString()
            readNextCodePointAndSkipWhitespace()
            check(expectedCodePoint(COLON)) { "'${COLON.toInt().toChar()}' expected" }
            return key
        }

        override fun readWithId(): Any? {
            skipWhitespace()
            return when {
                expectedCodePoint(QUOTE) -> StringEncoder.read(this)
                expectedCodePoint(LBRACKET) -> listEncoder.read(this)
                expectedCodePoint(LBRACE) -> {
                    val type = readKey()
                    skipQuote()
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
                        if (className.isNotEmpty()) readObject(encoder(className) as ClassUtf8Encoder) else {
                            check(expectedCodePoint(RBRACE)) { "'${RBRACE.toInt().toChar()}' expected" }
                            null
                        }
                    }
                }
                else -> error("unexpected codePoint $nextCodePoint")
            }
        }

        private fun readObject(encoder: ClassUtf8Encoder<*>): Any {
            properties = mutableMapOf()
            while (!expectedCodePoint(RBRACE)) {
                check(expectedCodePoint(COMMA)) { "'${COMMA.toInt().toChar()}' expected" }
                val name = readKey()
                readNextCodePointAndSkipWhitespace()
                val id = encoder.id(name)
                val value = if (id == NO_ENCODER_ID) with(TheReader(this, nextCodePoint)) { readWithId() } else {
                    check(expectedCodePoint(QUOTE)) { "'${QUOTE.toInt().toChar()}' expected" }
                    readNextCodePoint()
                    encoder(id).read(this)
                }
                readNextCodePointAndSkipWhitespace()
                encoder.addProperty(name, value)
            }
            return encoder.read(this)
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer).writeWithId(value)
    override fun read(reader: Reader): Any? = TheReader(reader, reader.readCodePoint()).readWithId()
}
