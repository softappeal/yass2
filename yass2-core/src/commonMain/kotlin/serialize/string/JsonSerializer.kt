package ch.softappeal.yass2.serialize.string

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val COMMA = ','.code
private const val COLON = ':'.code
private const val HASH = '#' // type
private const val LBRACKET = '['.code // list
private const val RBRACKET = ']'.code // list
private const val LBRACE = '{'.code // object
private const val RBRACE = '}'.code // object

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

        /** See [TheReader.readString]. */
        override fun checkString(string: String) {
            check(!string.contains(QUOTE.toChar())) { "'$string' must not contain '${QUOTE.toChar()}'" }
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
                    if (encoder is ClassStringEncoder) {
                        with(nested()) {
                            writeNewLine()
                            writeKey(HASH.toString())
                            writeQuoted(name)
                            encoder.write(this, value)
                            writeNewLine()
                        }
                        writeIndent()
                    } else {
                        writeKey("$HASH$name", false)
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

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint) {
        override fun readList() = buildList {
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

        override fun readString() = buildString {
            while (!expectedCodePoint(QUOTE)) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
        }

        fun readKey(): String {
            checkExpectedCodePoint(QUOTE)
            readNextCodePoint()
            val key = readString()
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
                val value = if (encoderId == NO_ENCODER_ID) readObject(this, nextCodePoint) else {
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
            expectedCodePoint(QUOTE) -> stringEncoder.read(this)
            expectedCodePoint(LBRACKET) -> listEncoder.read(this)
            expectedCodePoint(LBRACE) -> {
                readNextCodePointAndSkipWhitespace()
                if (expectedCodePoint(RBRACE)) null else {
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
                        val className = readString()
                        readNextCodePointAndSkipWhitespace()
                        readClass(encoder(className) as ClassStringEncoder)
                    }
                }
            }
            else -> error("unexpected codePoint $nextCodePoint")
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer, 0).writeObject(value)
    override fun read(reader: Reader): Any? = readObject(reader, reader.readCodePoint())
}
