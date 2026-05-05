@file:OptIn(InternalYassApi::class, TestingYassApi::class)

package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.InternalYassApi
import ch.softappeal.yass2.core.TestingYassApi
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer

private const val COLON = ':'.code
private const val LBRACKET = '['.code
private const val RBRACKET = ']'.code

public class TextSerializer(encoders: List<StringEncoder<*>>) : StringSerializer(encoders) {
    private inner class TheWriter(writer: Writer, indent: Int) : StringWriter(writer, indent) {
        override fun writeList(list: List<*>) {
            writeAsciiChar(LBRACKET)
            with(nested()) {
                list.forEach { element ->
                    writeNewLine()
                    writeIndent()
                    writeObject(element)
                }
            }
            writeNewLine()
            writeIndent()
            writeAsciiChar(RBRACKET)
        }

        private fun nested() = TheWriter(this, indent + 1)

        fun writeObject(value: Any?) {
            if (writeBuiltIn(value)) return
            val encoder = encoder(value!!::class)
            writeString(encoder.type.simpleName!!)
            writeAsciiChar(LPAREN)
            if (encoder !is ClassStringEncoder) write(encoder, value) else {
                writeNewLine()
                nested().write(encoder, value)
                writeIndent()
            }
            writeAsciiChar(RPAREN)
        }

        private fun writeProperty(name: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            writeIndent()
            writeString(name)
            writeAsciiChar(COLON)
            writeAsciiChar(SP)
            writeValue()
            writeNewLine()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name, value) { writeObject(value) }
        }

        override fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name, value) {
                if (!writeBuiltIn(value)) write(encoder(encoderId), value!!)
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint) {
        fun readList() = buildList {
            readNextCodePointAndSkipWhitespace()
            while (!expectedCodePoint(RBRACKET)) {
                add(readObject(nextCodePoint))
                readNextCodePointAndSkipWhitespace()
                if (expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
            }
        }

        fun readClass(encoder: ClassStringEncoder<*>): Any {
            while (!expectedCodePoint(RPAREN)) {
                val name = readString { expectedCodePoint(COLON) }
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val value = if (encoderId != STRING_NO_ENCODER_ID) encoder(encoderId).read(this) else {
                    readObject(nextCodePoint).apply { readNextCodePoint() }
                }
                skipWhitespace()
                encoder.addProperty(name, value)
                if (expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
            }
            return encoder.read(this)
        }
    }

    private fun Reader.readObject(nextCodePoint: Int): Any? = with(TheReader(this, nextCodePoint)) {
        skipWhitespace()
        if (expectedCodePoint(QUOTE)) return readQuotedString()
        if (expectedCodePoint(LBRACKET)) return readList()
        val (handled, result, className) = readBuiltIn { expectedCodePoint(LPAREN) }
        if (handled) return result
        readNextCodePointAndSkipWhitespace()
        val encoder = encoder(className)
        if (encoder is ClassStringEncoder) readClass(encoder) else encoder.read(this)
    }

    override fun Writer.write(value: Any?) {
        TheWriter(this, 0).writeObject(value)
    }

    override fun Reader.read(): Any? = readObject(readCodePoint())
}
