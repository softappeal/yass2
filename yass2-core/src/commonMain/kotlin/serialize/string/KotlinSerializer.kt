package ch.softappeal.yass2.serialize.string // TODO: review

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Writer

private const val COMMA = ','.code
private const val EQUALS = '='.code
private const val DOT = '.'.code
private const val PLUS = '+'.code
private const val LPAREN = '('.code
private const val RPAREN = ')'.code
private const val LBRACE = '{'.code
private const val RBRACE = '}'.code
private const val NULL = "null"
private const val LIST = "listOf"
private const val APPLY = "apply"

public class KotlinSerializer(encoders: List<StringEncoder<*>>) : StringSerializer(encoders) {
    private inner class TheWriter(writer: Writer, indent: Int) : StringWriter(writer, indent) {
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

        /** See [TheReader.readString]. */
        override fun checkString(string: String) {
            check(string.indexOfFirst { it.code.isWhitespace() || it.code == QUOTE || it.code == COMMA || it.code == LPAREN } < 0) {
                "'$string' must not contain whitespace, '${QUOTE.toChar()}', '${COMMA.toChar()}' or '${LPAREN.toChar()}'"
            }
        }

        private fun nested() = TheWriter(this, indent + 1)

        private var bodyProperties = false
        override fun startBodyProperties() {
            bodyProperties = true
            writeIndentMinus1()
            writeByte(RPAREN)
            writeByte(DOT)
            writeString(APPLY)
            writeByte(SP)
            writeByte(LBRACE)
            writeNewLine()
        }

        fun writeObject(value: Any?) {
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
                        writeByte(LPAREN)
                        writeByte(RPAREN)
                    } else {
                        writeByte(LPAREN)
                        if (encoder is ClassStringEncoder) {
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
            writeByte(SP)
            writeByte(EQUALS)
            writeByte(SP)
            writeValue()
            if (!bodyProperties) writeByte(COMMA)
            writeNewLine()
        }

        override fun writeProperty(name: String, value: Any?) {
            writeProperty(name) { writeObject(value) }
        }

        override fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name) {
                when (value) {
                    is Boolean,
                    is Int,
                    is Long,
                    is Double,
                        -> encoder(encoderId).write(this, value)
                    else -> {
                        if (value == null && encoderId != STRING_ENCODER_ID && encoderId != LIST_ENCODER_ID) writeByte(PLUS)
                        writeObject(value)
                    }
                }
            }
        }
    }

    private inner class TheReader(reader: Reader, nextCodePoint: Int) : StringReader(reader, nextCodePoint) {
        override fun readList() = buildList {
            readNextCodePointAndSkipWhitespace()
            while (!expectedCodePoint(RPAREN)) {
                add(readObject(this@TheReader, nextCodePoint))
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(COMMA)
                readNextCodePointAndSkipWhitespace()
            }
        }

        fun readUntil(isEnd: () -> Boolean) = buildString {
            while (!isEnd() && !isWhitespace()) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
            skipWhitespace()
        }

        private fun readUntil(end: Int) = readUntil { expectedCodePoint(end) }

        override fun readString() = readUntil { expectedCodePoint(QUOTE) || expectedCodePoint(COMMA) || expectedCodePoint(LPAREN) }

        fun readClass(encoder: ClassStringEncoder<*>): Any {
            fun properties(apply: Boolean) {
                while (!expectedCodePoint(if (apply) RBRACE else RPAREN)) {
                    val name = readUntil(EQUALS)
                    readNextCodePointAndSkipWhitespace()
                    val encoderId = encoder.encoderId(name)
                    var value: Any? = null
                    if (expectedCodePoint(PLUS)) {
                        readNextCodePointAndSkipWhitespace()
                        val className = readUntil { expectedCodePoint(LPAREN) || expectedCodePoint(DOT) }
                        check(className == NULL)
                        checkExpectedCodePoint(LPAREN)
                        readNextCodePointAndSkipWhitespace()
                        checkExpectedCodePoint(RPAREN)
                        readNextCodePoint()
                    } else {
                        value = when (val propertyEncoder = if (encoderId != NO_ENCODER_ID) encoder(encoderId) else null) {
                            is BooleanStringEncoder,
                            is IntStringEncoder,
                            is LongStringEncoder,
                            is DoubleStringEncoder,
                                -> propertyEncoder.read(this)
                            else -> readObject(this, nextCodePoint).apply { readNextCodePoint() }
                        }
                    }
                    skipWhitespace()
                    encoder.addNullableProperty(name, value)
                    if (!apply) {
                        checkExpectedCodePoint(COMMA)
                        readNextCodePointAndSkipWhitespace()
                    }
                }
            }
            properties(false)
            if (encoder.hasBodyProperties) {
                readNextCodePointAndSkipWhitespace()
                checkExpectedCodePoint(DOT)
                readNextCodePointAndSkipWhitespace()
                check(APPLY == readUntil(LBRACE)) { "'$APPLY' expected" }
                readNextCodePointAndSkipWhitespace()
                properties(true)
            }
            encoder.checkMissingProperties()
            return encoder.read(this)
        }
    }

    private fun readObject(reader: Reader, nextCodePoint: Int): Any? = with(TheReader(reader, nextCodePoint)) {
        skipWhitespace()
        if (expectedCodePoint(QUOTE)) return stringEncoder.read(this)
        val className = readUntil { expectedCodePoint(LPAREN) || expectedCodePoint(DOT) }
        if (className == LIST) {
            checkExpectedCodePoint(LPAREN)
            return listEncoder.read(this)
        }
        if (className == NULL) {
            checkExpectedCodePoint(LPAREN)
            readNextCodePointAndSkipWhitespace()
            checkExpectedCodePoint(RPAREN)
            return null
        }
        val encoder = encoder(className)
        if (expectedCodePoint(DOT)) {
            readNextCodePointAndSkipWhitespace()
            return (encoder as EnumStringEncoder).read(readString()).apply {
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

// NOTE: The functions below are needed for easy parsing of Kotlin source code.

public fun Boolean(string: String): Boolean = BooleanStringEncoder.read(string)
public fun Int(string: String): Int = IntStringEncoder.read(string)
public fun Long(string: String): Long = LongStringEncoder.read(string)
public fun Double(string: String): Double = DoubleStringEncoder.read(string)
public fun ByteArray(string: String): ByteArray = ByteArrayStringEncoder.read(string)

public operator fun <E : Enum<E>> E.invoke(): E = this
public operator fun Nothing?.invoke(): Nothing? = null
public operator fun Nothing?.unaryPlus(): Nothing? = null
