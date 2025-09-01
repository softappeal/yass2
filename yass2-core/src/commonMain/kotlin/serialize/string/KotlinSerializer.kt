@file:OptIn(InternalApi::class)

package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Writer

private const val EQUALS = '='.code
private const val DOT = '.'.code
private const val LIST = "listOf"

public class KotlinSerializer(encoders: List<StringEncoder<*>>) : StringSerializer(encoders) {
    private inner class TheWriter(writer: Writer, indent: Int) : StringWriter(writer, indent, true) {
        override fun writeList(list: List<*>) {
            writeString(LIST) // NOTE: only empty lists 'listOf()' of properties work; others need 'list<T>()'
            writeByte(LPAREN)
            with(nested()) {
                list.forEach { element ->
                    writeNewLine()
                    writeIndent()
                    writeObject(element)
                    writeByte(COMMA)
                }
            }
            writeNewLine()
            writeIndent()
            writeByte(RPAREN)
        }

        private fun nested() = TheWriter(this, indent + 1)

        fun writeObject(value: Any?) {
            if (writeBuiltIn(value)) return
            val encoder = encoder(value!!::class)
            writeString(encoder.type.simpleName!!)
            if (value is Enum<*>) {
                writeByte(DOT)
                writeString(value.name)
                writeByte(LPAREN)
                writeByte(RPAREN)
            } else {
                writeByte(LPAREN)
                if (encoder is ClassStringEncoder) {
                    writeNewLine()
                    with(nested()) { encoder.write(this, value) }
                    writeIndent()
                } else {
                    writeByte(QUOTE)
                    encoder.write(this, value)
                    writeByte(QUOTE)
                }
                writeByte(RPAREN)
            }
        }

        private fun writeProperty(name: String, writeValue: () -> Unit) {
            writeIndent()
            writeString(name)
            writeByte(SP)
            writeByte(EQUALS)
            writeByte(SP)
            writeValue()
            writeByte(COMMA)
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
                    is Double,
                        -> encoder(encoderId).write(this, value)
                    else -> writeObject(value)
                }
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint, true) {
        fun readList() = buildList {
            readNextCodePointAndSkipWhitespace()
            while (!expectedCodePoint(RPAREN)) {
                add(readObject(this@TheReader, nextCodePoint))
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(COMMA)
                readNextCodePointAndSkipWhitespace()
            }
        }

        fun readClass(encoder: ClassStringEncoder<*>): Any {
            while (!expectedCodePoint(RPAREN)) {
                val name = readUntil { expectedCodePoint(EQUALS) }
                readNextCodePointAndSkipWhitespace()
                val encoderId = encoder.encoderId(name)
                val value = when (val propertyEncoder = if (encoderId != STRING_NO_ENCODER_ID) encoder(encoderId) else null) {
                    is IntStringEncoder,
                    is LongStringEncoder,
                    is DoubleStringEncoder,
                        -> if (expectedCodePoint('n'.code)) {
                        readNextCodePoint()
                        checkExpectedCodePoint('u'.code)
                        readNextCodePoint()
                        checkExpectedCodePoint('l'.code)
                        readNextCodePoint()
                        checkExpectedCodePoint('l'.code)
                        readNextCodePoint()
                        null
                    } else propertyEncoder.read(this)
                    else -> readObject(this, nextCodePoint).apply { readNextCodePoint() }
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

    private fun readObject(reader: Reader, nextCodePoint: Int): Any? = with(TheReader(reader, nextCodePoint)) {
        skipWhitespace()
        if (expectedCodePoint(QUOTE)) return readStringBuiltIn()
        val (handled, result, className) = readUntilBuiltIn { expectedCodePoint(LPAREN) || expectedCodePoint(DOT) }
        if (handled) return result
        if (className == LIST) {
            checkExpectedCodePoint(LPAREN)
            return readList()
        }
        val encoder = encoder(className)
        if (expectedCodePoint(DOT)) {
            readNextCodePointAndSkipWhitespace()
            return (encoder as EnumStringEncoder).read(readUntil { expectedCodePoint(LPAREN) }).apply {
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

    override fun write(writer: Writer, value: Any?): Unit = TheWriter(writer, 0).writeObject(value)
    override fun read(reader: Reader): Any? = readObject(reader, reader.readCodePoint())
}

public operator fun <E : Enum<E>> E.invoke(): E = this // NOTE: needed for easy parsing of enums

/** NOTE: Provide a `constructor` for each [BaseStringEncoder] (needed for easy parsing of base types). */
public fun Int(string: String): Int = IntStringEncoder.read(string)
public fun Long(string: String): Long = LongStringEncoder.read(string)
public fun Double(string: String): Double = DoubleStringEncoder.read(string)
public fun ByteArray(string: String): ByteArray = ByteArrayStringEncoder.read(string)
