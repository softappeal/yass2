package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.NotJs
import ch.softappeal.yass2.serialize.Drainable
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val COMMA = ','.code
private const val EQUALS = '='.code
private const val DOT = '.'.code
private const val LPAREN = '('.code
private const val RPAREN = ')'.code
private const val LBRACE = '{'.code
private const val RBRACE = '}'.code
private const val NULL = "Null"
private const val LIST = "listOf"
private val ASSIGN = "${SP.toChar()}${EQUALS.toChar()}${SP.toChar()}".encodeToByteArray(throwOnInvalidSequence = true)
private val APPLY =
    "${RPAREN.toChar()}${DOT.toChar()}apply${SP.toChar()}${LBRACE.toChar()}".encodeToByteArray(throwOnInvalidSequence = true)

@Drainable
public class KotlinSerializer(encoders: List<Utf8Encoder<*>>) : Utf8Serializer(
    Utf8Encoder(List::class,
        { list ->
            writeString(LIST)
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
        },
        {
            ArrayList<Any?>(10).apply {
                readNextCodePointAndSkipWhitespace()
                while (!expectedCodePoint(RPAREN)) {
                    add(readObject())
                    readNextCodePointAndSkipWhitespace()
                    check(expectedCodePoint(COMMA)) { "'${COMMA.toChar()}' expected" }
                    readNextCodePointAndSkipWhitespace()
                }
            }
        }
    ),
    encoders,
) {

    private inner class TheWriter(writer: Writer, indent: Int) : Utf8Writer(writer, indent) {
        /** See [TheReader.readString]. */
        override fun checkString(string: String) { // TODO: correct implementation
            check(string.indexOfFirst { it.code.isWhitespace() || it.code == COMMA || it.code == RPAREN } < 0) {
                "'$string' must not contain whitespace, '${COMMA.toChar()}' or '${RPAREN.toChar()}'"
            }
        }

        override fun nested() = TheWriter(this, indent + 1)

        private var bodyProperties = false
        override fun startBodyProperties() {
            bodyProperties = true
            writeIndentMinus1()
            writeBytes(APPLY)
            writeNewLine()
        }

        override fun writeObject(value: Any?) {
            when (value) {
                null -> {
                    writeString(NULL)
                    writeByte(LPAREN)
                    writeByte(RPAREN)
                }
                is String -> stringEncoder.write(this, value)
                is List<*> -> listEncoder.write(this, value)
                else -> {
                    val encoder = encoder(value::class)
                    writeString(encoder.type.simpleName!!)
                    if (value is Enum<*>) {
                        writeByte(DOT)
                        writeString(value.name)
                    } else {
                        writeByte(LPAREN)
                        if (encoder is ClassUtf8Encoder) {
                            writeNewLine()
                            with(nested()) {
                                encoder.write(this, value)
                                writeIndentMinus1()
                                writeByte(if (bodyProperties) RBRACE else RPAREN)
                            }
                        } else {
                            writeByte(QUOTE)
                            encoder.write(this, value)
                            writeByte(QUOTE)
                            writeByte(RPAREN)
                        }
                    }
                }
            }
        }

        private fun writeProperty(name: String, writeValue: () -> Unit) {
            writeIndent()
            writeString(name)
            writeBytes(ASSIGN)
            writeValue()
            if (!bodyProperties) writeByte(COMMA)
            writeNewLine()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name) { writeObject(value) }
        }

        override fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name) {
                if (
                    value is Boolean ||
                    value is Int ||
                    value is Long ||
                    value is Double
                ) encoder(encoderId).write(this, value) else writeObject(value)
            }
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

        override fun readString() = readUntil { expectedCodePoint(QUOTE) || expectedCodePoint(COMMA) || expectedCodePoint(RPAREN) }

        override fun readObject(): Any? {
            skipWhitespace()
            if (expectedCodePoint(QUOTE)) return stringEncoder.read(this)
            val className = readUntil { expectedCodePoint(LPAREN) || expectedCodePoint(DOT) }
            if (className == LIST) {
                check(expectedCodePoint(LPAREN)) { "'${LPAREN.toChar()}' expected" }
                return listEncoder.read(this)
            }
            if (className == NULL) {
                check(expectedCodePoint(LPAREN)) { "'${LPAREN.toChar()}' expected" }
                readNextCodePointAndSkipWhitespace()
                check(expectedCodePoint(RPAREN)) { "'${RPAREN.toChar()}' expected" }
                return null
            }
            val encoder = encoder(className)
            if (expectedCodePoint(DOT)) {
                readNextCodePointAndSkipWhitespace()
                return (encoder as EnumUtf8Encoder).read(readString())
            }
            check(expectedCodePoint(LPAREN)) { "'${LPAREN.toChar()}' expected" }
            readNextCodePointAndSkipWhitespace()
            return if (encoder is ClassUtf8Encoder) {
                readClass(encoder)
            } else {
                check(expectedCodePoint(QUOTE)) { "'${QUOTE.toChar()}' expected" }
                readNextCodePoint()
                encoder.read(this).apply {
                    check(expectedCodePoint(QUOTE)) { "'${QUOTE.toChar()}' expected" }
                    readNextCodePointAndSkipWhitespace()
                    check(expectedCodePoint(RPAREN)) { "'${RPAREN.toChar()}' expected" }
                }
            }
        }

        private fun readClass(encoder: ClassUtf8Encoder<*>): Any {
            while (!expectedCodePoint(RPAREN)) {
                val name = readUntil(EQUALS)
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

@Suppress("FunctionName") public fun Null(): Nothing? = null
public fun Boolean(string: String): Boolean = BooleanUtf8Encoder.read(string)
public fun Int(string: String): Int = IntUtf8Encoder.read(string)
public fun Long(string: String): Long = LongUtf8Encoder.read(string)
@NotJs public fun Double(string: String): Double = DoubleUtf8Encoder.read(string)
public fun ByteArray(string: String): ByteArray = ByteArrayUtf8Encoder.read(string)
