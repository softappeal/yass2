@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer

private const val COLON = ':'.code
private const val HASH = '#' // type
private const val LBRACKET = '['.code
private const val RBRACKET = ']'.code
private const val LBRACE = '{'.code
private const val RBRACE = '}'.code

public class JsonSerializer(encoders: List<StringEncoder<*>>) : StringSerializer(encoders) {
    private inner class TheWriter(writer: Writer, indent: Int) : StringWriter(writer, indent) {
        override fun writeList(list: List<*>) {
            writeByte(LBRACKET)
            with(nested()) {
                list.forEachIndexed { index, element ->
                    if (index != 0) writeByte(COMMA)
                    writeNewLine()
                    writeIndent()
                    writeObject(element)
                }
            }
            writeNewLine()
            writeIndent()
            writeByte(RBRACKET)
        }

        private fun nested() = TheWriter(this, indent + 1)

        private fun writeQuoted(string: String) {
            writeByte(QUOTE)
            writeString(string)
            writeByte(QUOTE)
        }

        private fun writeKey(key: String, expanded: Boolean = true) {
            if (expanded) writeIndent()
            writeQuoted(key)
            writeByte(COLON)
            if (expanded) writeByte(SP)
        }

        private fun StringEncoder<*>.writeQuoted(value: Any?) {
            writeByte(QUOTE)
            write(this@TheWriter, value)
            writeByte(QUOTE)
        }

        fun writeObject(value: Any?) {
            if (writeBuiltIn(value)) return
            writeByte(LBRACE)
            val encoder = encoder(value!!::class)
            val className = encoder.type.simpleName!!
            if (encoder is ClassStringEncoder) {
                with(nested()) {
                    writeNewLine()
                    writeKey(HASH.toString())
                    writeQuoted(className)
                    encoder.write(this, value)
                    writeNewLine()
                }
                writeIndent()
            } else {
                writeKey("$HASH$className", false)
                encoder.writeQuoted(value)
            }
            writeByte(RBRACE)
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
                if (writePropertyBuiltIn(value, encoderId)) encoder(encoderId).writeQuoted(value)
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint) {
        fun readList(): List<*> = buildList {
            readNextCodePointAndSkipWhitespace()
            var first = true
            while (!expectedCodePoint(RBRACKET)) {
                if (first) first = false else {
                    checkExpectedCodePoint(COMMA)
                    readNextCodePointAndSkipWhitespace()
                }
                add(readObject(this@TheReader, nextCodePoint))
                readNextCodePointAndSkipWhitespace()
            }
        }

        fun readKey(): String {
            checkExpectedCodePoint(QUOTE)
            readNextCodePoint()
            val key = readBaseString()
            readNextCodePointAndSkipWhitespace()
            checkExpectedCodePoint(COLON)
            return key
        }

        fun readClass(encoder: ClassStringEncoder<*>): Any {
            while (!expectedCodePoint(RBRACE)) {
                checkExpectedCodePoint(COMMA)
                readNextCodePointAndSkipWhitespace()
                val name = readKey()
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val value = if (encoderId == STRING_NO_ENCODER_ID) readObject(this, nextCodePoint) else {
                    checkExpectedCodePoint(QUOTE)
                    readNextCodePoint()
                    encoder(encoderId).read(this)
                }
                readNextCodePointAndSkipWhitespace()
                encoder.addProperty(name, value)
            }
            return encoder.read(this)
        }
    }

    private fun readObject(reader: Reader, nextCodePoint: Int) = with(TheReader(reader, nextCodePoint)) {
        skipWhitespace()
        when {
            expectedCodePoint(QUOTE) -> readStringBuiltIn()
            expectedCodePoint(LBRACKET) -> readList()
            expectedCodePoint(LBRACE) -> {
                readNextCodePointAndSkipWhitespace()
                val type = readKey()
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(QUOTE)
                readNextCodePoint()
                check(type.isNotEmpty()) { "empty type" }
                check(type[0] == HASH) { "'$HASH' expected" }
                if (type.length > 1) {
                    val className = type.substring(1)
                    val encoder = encoder(className)
                    check(encoder !is ClassStringEncoder) { "is ClassStringEncoder" }
                    val value = encoder.read(this)
                    checkExpectedCodePoint(QUOTE)
                    readNextCodePointAndSkipWhitespace()
                    checkExpectedCodePoint(RBRACE)
                    value
                } else {
                    val className = readBaseString()
                    readNextCodePointAndSkipWhitespace()
                    readClass(encoder(className) as ClassStringEncoder)
                }
            }
            else -> {
                val (handled, result, _) = readUntilBuiltIn { false }
                if (!handled) error("unexpected codePoint $nextCodePoint")
                result
            }
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer, 0).writeObject(value)
    override fun read(reader: Reader): Any? = readObject(reader, reader.readCodePoint())
}
