package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.serialize.BytesReader
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.text.TextSerializer.TextReader
import ch.softappeal.yass2.serialize.text.TextSerializer.TextWriter
import kotlin.reflect.KClass

public fun Writer.writeTextBytes(string: String) {
    writeBytes(string.encodeToByteArray(throwOnInvalidSequence = true))
}

public open class TextEncoder<T : Any>(
    internal val type: KClass<T>,
    internal val write: TextWriter.(value: T) -> Unit,
    internal val read: TextReader.() -> T,
) {
    internal fun write(writer: TextWriter, value: Any?) = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    internal fun read(reader: TextReader) = reader.read()
}

public class ClassTextEncoder<T : Any>(
    type: KClass<T>,
    write: TextWriter.(value: T) -> Unit,
    read: TextReader.() -> T,
    vararg propertyId: Pair<String, Int>,
) : TextEncoder<T>(type, write, read) {
    internal val property2id = propertyId.toMap()
}

// The following constants are ASCII characters and therefore encoded in one byte in UTF-8.
public const val TEXT_COMMA: Byte = ','.code.toByte()
public const val TEXT_LBRACKET: Byte = '['.code.toByte()
public const val TEXT_RBRACKET: Byte = ']'.code.toByte()
public const val TEXT_LPAREN: Byte = '('.code.toByte()
public const val TEXT_RPAREN: Byte = ')'.code.toByte()
public const val TEXT_ASTERIX: Byte = '*'.code.toByte()
public const val TEXT_COLON: Byte = ':'.code.toByte()
public const val TEXT_QUOTE: Byte = '"'.code.toByte()
public const val TEXT_BS: Byte = '\\'.code.toByte()

private const val BS = TEXT_BS.toInt().toChar().toString()
private const val BS_BS = BS + BS
private const val QUOTE = TEXT_QUOTE.toInt().toChar().toString()
private const val BS_QUOTE = BS + QUOTE

public const val TEXT_STRING_ENCODER_ID: Int = 0
public const val TEXT_LIST_ENCODER_ID: Int = 1
public const val TEXT_FIRST_ENCODER_ID: Int = 2

private val Tab = ByteArray(4) { ' '.code.toByte() }

/**
 * It reads/writes UTF-8 encoded strings.
 * Has built-in encoders for null, [String] and [List].
 * Concrete classes must be concrete and must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
public abstract class TextSerializer(private val multilineWrite: Boolean) : Serializer {
    private lateinit var encoders: Array<TextEncoder<*>>
    private lateinit var type2encoder: MutableMap<KClass<*>, TextEncoder<*>>
    private lateinit var className2encoder: MutableMap<String, TextEncoder<*>>

    protected fun initialize(vararg encoders: TextEncoder<*>) {
        this.encoders = (listOf(stringEncoder, listEncoder) + encoders).toTypedArray()
        type2encoder = HashMap(this.encoders.size)
        className2encoder = HashMap(this.encoders.size)
        this.encoders.forEach { encoder ->
            require(type2encoder.put(encoder.type, encoder) == null) { "duplicated type '${encoder.type}'" }
            require(className2encoder.put(encoder.type.simpleName!!, encoder) == null) {
                "duplicated className '${encoder.type.simpleName}'"
            }
        }
    }

    private val stringEncoder = TextEncoder(String::class,
        { string ->
            writeByte(TEXT_QUOTE)
            writeTextBytes(string.replace(BS, BS_BS).replace(QUOTE, BS_QUOTE))
            writeByte(TEXT_QUOTE)
        },
        {
            buildString {
                while (true) {
                    readNextCodePoint()
                    when {
                        expectedCodePoint(TEXT_QUOTE) -> break
                        expectedCodePoint(TEXT_BS) -> {
                            readNextCodePoint()
                            when {
                                expectedCodePoint(TEXT_QUOTE) -> append(TEXT_QUOTE.toInt().toChar())
                                expectedCodePoint(TEXT_BS) -> append(TEXT_BS.toInt().toChar())
                                else -> error("illegal escape with codePoint $nextCodePoint")
                            }
                        }
                        else -> addCodePoint(nextCodePoint)
                    }
                }
            }
        }
    )

    private val listEncoder = TextEncoder(List::class,
        { list ->
            writeByte(TEXT_LBRACKET)
            writeNewLine()
            with(nested()) {
                list.forEachIndexed { index, element ->
                    if (!multilineWrite && index != 0) writeByte(TEXT_COMMA)
                    writeIndent()
                    writeWithId(element)
                    writeNewLine()
                }
            }
            writeIndent()
            writeByte(TEXT_RBRACKET)
        },
        {
            ArrayList<Any?>(10).apply {
                readNextCodePoint()
                skipWhitespace()
                while (!expectedCodePoint(TEXT_RBRACKET)) {
                    add(readWithId())
                    readNextCodePoint()
                    skipWhitespace()
                    if (expectedCodePoint(TEXT_COMMA)) readNextCodePoint()
                    skipWhitespace()
                }
            }
        }
    )

    private fun Writer.writeNewLine() {
        if (multilineWrite) writeByte('\n'.code.toByte())
    }

    public inner class TextWriter(private val writer: Writer, private val indent: Int = 0) : Writer by writer {
        internal fun nested() = if (multilineWrite) TextWriter(this, indent + 1) else this

        internal fun writeIndent() {
            if (multilineWrite) repeat(indent) { writeBytes(Tab) }
        }

        internal fun writeWithId(value: Any?) {
            when (value) {
                null -> writeByte(TEXT_ASTERIX)
                is String -> stringEncoder.write(this, value)
                is List<*> -> listEncoder.write(this, value)
                else -> {
                    val encoder = type2encoder[value::class] ?: error("missing type '${value::class}'")
                    writeTextBytes(encoder.type.simpleName!!)
                    writeByte(TEXT_LPAREN)
                    if (encoder !is ClassTextEncoder) encoder.write(this, value) else {
                        writeNewLine()
                        with(nested()) { encoder.write(this, value) }
                        writeIndent()
                    }
                    writeByte(TEXT_RPAREN)
                }
            }
        }

        private var firstProperty: Boolean = true
        private fun writeProperty(property: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            if (multilineWrite || firstProperty) firstProperty = false else writeByte(TEXT_COMMA)
            writeIndent()
            writeTextBytes(property)
            writeByte(TEXT_COLON)
            if (multilineWrite) writeByte(' '.code.toByte())
            writeValue()
            writeNewLine()
        }

        public fun writeWithId(property: String, value: Any?) {
            writeProperty(property, value) { TextWriter(this, indent).writeWithId(value) }
        }

        public fun writeNoId(property: String, id: Int, value: Any?) {
            writeProperty(property, value) { encoders[id].write(this, value) }
        }
    }

    public inner class TextReader(private val reader: Reader, internal var nextCodePoint: Int) : Reader by reader {
        internal fun expectedCodePoint(codePoint: Byte) = nextCodePoint == codePoint.toInt()

        internal fun readNextCodePoint() {
            nextCodePoint = readCodePoint()
        }

        private fun isWhitespace() =
            expectedCodePoint(32) || // SPACE
                expectedCodePoint(9) || // TAB
                expectedCodePoint(10) || // LF
                expectedCodePoint(13) // CR

        internal fun skipWhitespace() {
            while (isWhitespace()) readNextCodePoint()
        }

        private fun readUntil(predicate: () -> Boolean) = buildString {
            while (!predicate() && !isWhitespace()) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
            skipWhitespace()
        }

        private fun readDelimiter(delimiter: Byte) = readUntil { expectedCodePoint(delimiter) }

        /**
         * Reads until [TEXT_RPAREN] or [TEXT_COMMA].
         */
        public fun readTextBytes(): String = readUntil { expectedCodePoint(TEXT_RPAREN) || expectedCodePoint(TEXT_COMMA) }

        internal fun readWithId(): Any? {
            skipWhitespace()
            return when {
                expectedCodePoint(TEXT_ASTERIX) -> null
                expectedCodePoint(TEXT_QUOTE) -> stringEncoder.read(this)
                expectedCodePoint(TEXT_LBRACKET) -> listEncoder.read(this)
                else -> {
                    val className = readDelimiter(TEXT_LPAREN)
                    val encoder = className2encoder[className] ?: error("missing encoder for class '${className}'")
                    readNextCodePoint()
                    skipWhitespace()
                    if (encoder is ClassTextEncoder) readObject(encoder) else encoder.read(this)
                }
            }
        }

        private lateinit var properties: MutableMap<String, Any?>

        private fun readObject(encoder: ClassTextEncoder<*>): Any {
            properties = mutableMapOf()
            while (!expectedCodePoint(TEXT_RPAREN)) {
                val propertyName = readDelimiter(TEXT_COLON)
                readNextCodePoint()
                skipWhitespace()
                val id = encoder.property2id[propertyName]
                check(properties.put(propertyName, if (id != null) encoders[id].read(this) else {
                    with(TextReader(this, nextCodePoint)) { readWithId() }.apply { readNextCodePoint() }
                }) == null) { "duplicated property '$propertyName' for type '${encoder.type.simpleName}'" }
                skipWhitespace()
                if (expectedCodePoint(TEXT_COMMA)) readNextCodePoint()
                skipWhitespace()
            }
            return encoder.read(this)
        }

        public fun getProperty(property: String): Any? {
            return properties[property]
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TextWriter(writer).writeWithId(value)
    override fun read(reader: Reader): Any? = TextReader(reader, reader.readCodePoint()).readWithId()
}

public fun TextSerializer.writeString(value: Any?): String = with(BytesWriter(1000)) {
    write(this, value)
    buffer.decodeToString(0, current, throwOnInvalidSequence = true)
}

public fun TextSerializer.readString(string: String): Any? =
    with(BytesReader(string.encodeToByteArray(throwOnInvalidSequence = true))) {
        read(this).apply { check(isDrained) }
    }
