@file:OptIn(InternalApi::class, TestingApi::class)

package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.TestingApi
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer

private const val EQUALS = '='.code
private const val DOT = '.'.code
private const val LIST = "listOf"

public class KotlinSerializer(encoders: List<StringEncoder<*>>) : StringSerializer(encoders) {
    private inner class TheWriter(writer: Writer, indent: Int) : StringWriter(writer, indent, true) {
        override fun writeList(list: List<*>) {
            writeString(LIST) // NOTE: only empty lists 'listOf()' of properties work; others need 'list<T>()'
            writeAsciiChar(LPAREN)
            with(nested()) {
                list.forEach { element ->
                    writeNewLine()
                    writeIndent()
                    writeObject(element)
                    writeAsciiChar(COMMA)
                }
            }
            writeNewLine()
            writeIndent()
            writeAsciiChar(RPAREN)
        }

        private fun nested() = TheWriter(this, indent + 1)

        fun writeObject(value: Any?) {
            if (writeBuiltIn(value)) return
            val encoder = encoder(value!!::class)
            writeString(encoder.type.simpleName!!)
            if (value is Enum<*>) {
                writeAsciiChar(DOT)
                writeString(value.name)
                writeAsciiChar(LPAREN)
                writeAsciiChar(RPAREN)
            } else {
                writeAsciiChar(LPAREN)
                if (encoder is ClassStringEncoder) {
                    writeNewLine()
                    nested().write(encoder, value)
                    writeIndent()
                } else {
                    writeAsciiChar(QUOTE)
                    write(encoder, value)
                    writeAsciiChar(QUOTE)
                }
                writeAsciiChar(RPAREN)
            }
        }

        private fun writeProperty(name: String, writeValue: () -> Unit) {
            writeIndent()
            writeString(name)
            writeAsciiChar(SP)
            writeAsciiChar(EQUALS)
            writeAsciiChar(SP)
            writeValue()
            writeAsciiChar(COMMA)
            writeNewLine()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name) { writeObject(value) }
        }

        override fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name) {
                when (value) {
                    is Int,
                    is Long,
                        -> write(encoder(encoderId), value)
                    else -> writeObject(value)
                }
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint, true) {
        fun readList() = buildList {
            readNextCodePointAndSkipWhitespace()
            while (!expectedCodePoint(RPAREN)) {
                add(readObject(nextCodePoint))
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(COMMA)
                readNextCodePointAndSkipWhitespace()
            }
        }

        fun readClass(encoder: ClassStringEncoder<*>): Any {
            while (!expectedCodePoint(RPAREN)) {
                val name = readString { expectedCodePoint(EQUALS) }
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val propertyEncoder = if (encoderId != STRING_NO_ENCODER_ID) encoder(encoderId) else null
                val value = when (propertyEncoder) {
                    is IntStringEncoder,
                    is LongStringEncoder,
                        -> {
                        if (expectedCodePoint('n'.code)) {
                            readNextCodePoint()
                            checkExpectedCodePoint('u'.code)
                            readNextCodePoint()
                            checkExpectedCodePoint('l'.code)
                            readNextCodePoint()
                            checkExpectedCodePoint('l'.code)
                            readNextCodePoint()
                            null
                        } else propertyEncoder.read(this)
                    }
                    else -> readObject(nextCodePoint).apply { readNextCodePoint() }
                }
                skipWhitespace()
                encoder.addProperty(name, value)
                checkExpectedCodePoint(COMMA)
                readNextCodePointAndSkipWhitespace()
            }
            encoder.checkMissingProperties()
            return encoder.read(this)
        }
    }

    private fun Reader.readObject(nextCodePoint: Int): Any? = with(TheReader(this, nextCodePoint)) {
        skipWhitespace()
        if (expectedCodePoint(QUOTE)) return readQuotedString()
        val (handled, result, className) = readBuiltIn { expectedCodePoint(LPAREN) || expectedCodePoint(DOT) }
        if (handled) return result
        if (className == LIST) {
            checkExpectedCodePoint(LPAREN)
            return readList()
        }
        val encoder = encoder(className)
        if (expectedCodePoint(DOT)) {
            readNextCodePointAndSkipWhitespace()
            return (encoder as EnumStringEncoder).readBase(readString { expectedCodePoint(LPAREN) }).apply {
                checkExpectedCodePoint(LPAREN)
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(RPAREN)
            }
        }
        checkExpectedCodePoint(LPAREN)
        readNextCodePointAndSkipWhitespace()
        return if (encoder is ClassStringEncoder) readClass(encoder) else {
            checkExpectedCodePoint(QUOTE)
            readNextCodePoint()
            encoder.read(this).apply {
                checkExpectedCodePoint(QUOTE)
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(RPAREN)
            }
        }
    }

    override fun Writer.write(value: Any?) {
        TheWriter(this, 0).writeObject(value)
    }

    override fun Reader.read(): Any? = readObject(readCodePoint())
}

public operator fun <E : Enum<E>> E.invoke(): E = this // NOTE: needed for easy parsing of enums

/** NOTE: Provide a `constructor` for each [BaseStringEncoder] (needed for easy parsing of base types). */
public fun Int(string: String): Int = IntStringEncoder.readBase(string)
public fun Long(string: String): Long = LongStringEncoder.readBase(string)
public fun ByteArray(string: String): ByteArray = ByteArrayStringEncoder.readBase(string)
