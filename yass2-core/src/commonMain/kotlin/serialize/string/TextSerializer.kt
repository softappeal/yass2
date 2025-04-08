package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer

private const val COLON = ':'.code
private const val LBRACKET = '['.code
private const val RBRACKET = ']'.code

public class TextSerializer(encoders: List<StringEncoder<*>>) : StringSerializer(encoders) {
    private inner class TheWriter(writer: Writer, indent: Int) : StringWriter(writer, indent) {
        override fun writeList(list: List<*>) {
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
        }

        private fun nested() = TheWriter(this, indent + 1)

        fun writeObject(value: Any?) {
            if (writeBuiltIn(value)) return
            val encoder = encoder(value!!::class)
            writeString(encoder.type.simpleName!!)
            writeByte(LPAREN)
            if (encoder !is ClassStringEncoder) encoder.write(this, value) else {
                writeNewLine()
                encoder.write(nested(), value)
                writeIndent()
            }
            writeByte(RPAREN)
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
            writeProperty(name, value) {
                if (writePropertyBuiltIn(value, encoderId)) encoder(encoderId).write(this, value)
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint) {
        fun readList() = buildList {
            readNextCodePointAndSkipWhitespace()
            while (!expectedCodePoint(RBRACKET)) {
                add(readObject(this@TheReader, nextCodePoint))
                readNextCodePointAndSkipWhitespace()
                if (expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
            }
        }

        fun readClass(encoder: ClassStringEncoder<*>): Any {
            while (!expectedCodePoint(RPAREN)) {
                val name = readUntil { expectedCodePoint(COLON) }
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val value = if (encoderId != NO_ENCODER_ID) encoder(encoderId).read(this) else {
                    readObject(this, nextCodePoint).apply { readNextCodePoint() }
                }
                skipWhitespace()
                encoder.addProperty(name, value)
                if (expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
            }
            return encoder.read(this)
        }
    }

    private fun readObject(reader: Reader, nextCodePoint: Int): Any? = with(TheReader(reader, nextCodePoint)) {
        skipWhitespace()
        if (expectedCodePoint(QUOTE)) return readStringBuiltIn()
        if (expectedCodePoint(LBRACKET)) return readList()
        val (handled, result, className) = readUntilBuiltIn { expectedCodePoint(LPAREN) }
        if (handled) return result
        readNextCodePointAndSkipWhitespace()
        val encoder = encoder(className)
        if (encoder is ClassStringEncoder) readClass(encoder) else encoder.read(this)
    }

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer, 0).writeObject(value)
    override fun read(reader: Reader): Any? = readObject(reader, reader.readCodePoint())
}
