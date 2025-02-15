package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val COMMA = ','.code
private const val COLON = ':'.code
private const val ASTERIX = '*'.code // null
private const val LBRACKET = '['.code // list
private const val RBRACKET = ']'.code // list
private const val LPAREN = '('.code // object
private const val RPAREN = ')'.code // object

public class TextSerializer(encoders: List<Utf8Encoder<*>>) : Utf8Serializer(
    Utf8Encoder(List::class,
        { list ->
            writeByte(LBRACKET)
            with(nested()) {
                list.forEach { element ->
                    writeNewLine()
                    writeIndent()
                    writeObject(element)
                }
            }
            writeNewLine()
            writeIndent()
            writeByte(RBRACKET)
        },
        {
            ArrayList<Any?>(10).apply {
                readNextCodePointAndSkipWhitespace()
                while (!expectedCodePoint(RBRACKET)) {
                    add(readObject())
                    readNextCodePointAndSkipWhitespace()
                    if (expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
                }
            }
        }
    ),
    encoders,
) {

    private inner class TheWriter(writer: Writer, indent: Int) : Utf8Writer(writer, indent) {
        /** See [TheReader.readString]. */
        override fun checkString(string: String) {
            check(string.indexOfFirst { it.code.isWhitespace() || it.code == COMMA || it.code == RPAREN } < 0) {
                "'$string' must not contain whitespace, '${COMMA.toChar()}' or '${RPAREN.toChar()}'"
            }
        }

        override fun nested() = TheWriter(this, indent + 1)

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
                        encoder.write(nested(), value)
                        writeIndent()
                    }
                    writeByte(RPAREN)
                }
            }
        }

        private fun writeProperty(name: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            writeIndent()
            writeString(name)
            writeByte(COLON)
            writeByte(SP)
            writeValue()
            writeNewLine()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name, value) { writeObject(value) }
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

        private fun readUntil(end: Int) = readUntil { expectedCodePoint(end) }

        override fun readString() = readUntil { expectedCodePoint(COMMA) || expectedCodePoint(RPAREN) }

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

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer, 0).writeObject(value)
    override fun read(reader: Reader): Any? = TheReader(reader, reader.readCodePoint()).readObject()
}
