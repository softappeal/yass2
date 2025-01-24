package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.readBytes
import ch.softappeal.yass2.serialize.utf8.Utf8Serializer.Utf8Writer
import ch.softappeal.yass2.serialize.writeBytes
import kotlin.reflect.KClass

private const val S_BS = Utf8Serializer.BS.toInt().toChar().toString()
private const val S_BS_BS = S_BS + S_BS
private const val S_QUOTE = Utf8Serializer.QUOTE.toInt().toChar().toString()
private const val S_BS_QUOTE = S_BS + S_QUOTE
private const val S_NL = Utf8Serializer.NL.toInt().toChar().toString()
private const val S_BS_NL = S_BS + "n"
private const val S_CR = Utf8Serializer.CR.toInt().toChar().toString()
private const val S_BS_CR = S_BS + "r"
private const val S_TAB = Utf8Serializer.TAB.toInt().toChar().toString()
private const val S_BS_TAB = S_BS + "t"

private val Tab = ByteArray(4) { Utf8Serializer.SP }

public abstract class Utf8Reader(private val reader: Reader, private var _nextCodePoint: Int) : Reader by reader {
    public val nextCodePoint: Int get() = _nextCodePoint
    public fun expectedCodePoint(codePoint: Byte): Boolean = _nextCodePoint == codePoint.toInt()
    public fun readNextCodePoint() {
        _nextCodePoint = readCodePoint()
    }

    protected fun isWhitespace(): Boolean =
        expectedCodePoint(Utf8Serializer.SP) ||
            expectedCodePoint(Utf8Serializer.TAB) ||
            expectedCodePoint(Utf8Serializer.NL) ||
            expectedCodePoint(Utf8Serializer.CR)

    protected fun skipWhitespace() {
        while (isWhitespace()) readNextCodePoint()
    }

    public fun readNextCodePointAndSkipWhitespace() {
        readNextCodePoint()
        skipWhitespace()
    }

    public abstract fun readString(): String
    public abstract fun readWithId(): Any?

    protected lateinit var properties: MutableMap<String, Any>

    protected fun Utf8Encoder<*>.addProperty(name: String, value: Any?) {
        check(value != null) { "property '${type.simpleName}.$name' must not be explicitly set to null" }
        check(properties.put(name, value) == null) { "duplicated property '${type.simpleName}.$name'" }
    }

    public fun getProperty(property: String): Any? = properties[property]
}

public open class Utf8Encoder<T : Any>(
    public val type: KClass<T>,
    private val write: Utf8Writer.(value: T) -> Unit,
    private val read: Utf8Reader.() -> T,
) {
    public fun write(writer: Utf8Writer, value: Any?): Unit = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    public fun read(reader: Utf8Reader): T = reader.read()
}

public class ClassUtf8Encoder<T : Any>(
    type: KClass<T>,
    write: Utf8Writer.(value: T) -> Unit,
    read: Utf8Reader.() -> T,
    /** see [Utf8Serializer.NO_ENCODER_ID] */
    vararg propertyId: Pair<String, Int>,
) : Utf8Encoder<T>(type, write, read) {
    private val property2id = propertyId.toMap()
    public fun id(name: String): Int {
        val id = property2id[name]
        check(id != null) { "no property '${type.simpleName}.$name'" }
        return id
    }
}

/**
 * It reads/writes UTF-8 encoded strings.
 * Has built-in encoders for null, [String] and [List].
 */
public abstract class Utf8Serializer(
    utf8Encoders: List<Utf8Encoder<*>>,
    protected val multilineWrite: Boolean,
    private val strictListComma: Boolean,
) : Serializer {
    private var indent: Int = 0

    public abstract inner class Utf8Writer(private val writer: Writer) : Writer by writer {
        public fun writeString(string: String) {
            writeBytes(string.encodeToByteArray(throwOnInvalidSequence = true))
        }

        public fun writeIndent() {
            if (multilineWrite) repeat(indent) { writeBytes(Tab) }
        }

        public fun nested(action: () -> Unit) {
            indent++
            try {
                action()
            } finally {
                indent--
            }
        }

        public fun writeNewLine() {
            if (multilineWrite) writeByte(NL)
        }

        public abstract fun writeWithId(property: String, value: Any?)
        public abstract fun writeNoId(property: String, id: Int, value: Any?)
        public abstract fun writeWithId(value: Any?)
    }

    protected val stringEncoder: Utf8Encoder<String> = Utf8Encoder(String::class,
        { string ->
            writeByte(QUOTE)
            writeString(
                string
                    .replace(S_BS, S_BS_BS) // must be first!
                    .replace(S_QUOTE, S_BS_QUOTE)
                    .replace(S_NL, S_BS_NL)
                    .replace(S_CR, S_BS_CR)
                    .replace(S_TAB, S_BS_TAB)
            )
            writeByte(QUOTE)
        },
        {
            buildString {
                while (true) {
                    readNextCodePoint()
                    when {
                        expectedCodePoint(QUOTE) -> break
                        expectedCodePoint(BS) -> {
                            readNextCodePoint()
                            when {
                                expectedCodePoint(QUOTE) -> append(QUOTE.toInt().toChar())
                                expectedCodePoint(BS) -> append(BS.toInt().toChar())
                                expectedCodePoint('n'.code.toByte()) -> append(NL.toInt().toChar())
                                expectedCodePoint('r'.code.toByte()) -> append(CR.toInt().toChar())
                                expectedCodePoint('t'.code.toByte()) -> append(TAB.toInt().toChar())
                                else -> error("illegal escape with codePoint $nextCodePoint")
                            }
                        }
                        else -> addCodePoint(nextCodePoint)
                    }
                }
            }
        }
    )

    protected val listEncoder: Utf8Encoder<List<*>> = Utf8Encoder(List::class,
        { list ->
            writeByte(LBRACKET)
            nested {
                list.forEachIndexed { index, element ->
                    if ((!multilineWrite || strictListComma) && index != 0) writeByte(COMMA)
                    writeNewLine()
                    writeIndent()
                    writeWithId(element)
                }
            }
            writeNewLine()
            writeIndent()
            writeByte(RBRACKET)
        },
        {
            ArrayList<Any?>(10).apply {
                readNextCodePointAndSkipWhitespace()
                var first = true
                while (!expectedCodePoint(RBRACKET)) {
                    if (strictListComma) {
                        if (first) first = false else {
                            check(expectedCodePoint(COMMA)) { "'${COMMA.toInt().toChar()}' expected" }
                            readNextCodePointAndSkipWhitespace()
                        }
                    }
                    add(readWithId())
                    readNextCodePointAndSkipWhitespace()
                    if (!strictListComma && expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
                }
            }
        }
    )

    public companion object {
        public const val NO_ENCODER_ID: Int = -1

        public const val QUOTE: Byte = '"'.code.toByte()
        public const val LBRACKET: Byte = '['.code.toByte()
        public const val COMMA: Byte = ','.code.toByte()
        public const val SP: Byte = ' '.code.toByte()
        public const val COLON: Byte = ':'.code.toByte()
        internal const val BS = '\\'.code.toByte()
        internal const val NL = '\n'.code.toByte()
        internal const val CR = '\r'.code.toByte()
        internal const val TAB = '\t'.code.toByte()
        private const val RBRACKET = ']'.code.toByte()

        public const val STRING_ENCODER_ID: Int = 0
        public const val LIST_ENCODER_ID: Int = 1
        public const val FIRST_ENCODER_ID: Int = 2
    }

    private val encoders = (listOf(stringEncoder, listEncoder) + utf8Encoders).toTypedArray()
    private val type2encoder = HashMap<KClass<*>, Utf8Encoder<*>>(encoders.size)
    private val className2encoder = HashMap<String, Utf8Encoder<*>>(encoders.size)

    init {
        encoders.forEach { encoder ->
            require(type2encoder.put(encoder.type, encoder) == null) { "duplicated type '${encoder.type}'" }
            require(className2encoder.put(encoder.type.simpleName!!, encoder) == null) {
                "duplicated className '${encoder.type.simpleName}'"
            }
        }
    }

    protected fun encoder(index: Int): Utf8Encoder<*> = encoders[index]
    protected fun encoder(type: KClass<*>): Utf8Encoder<*> = type2encoder[type] ?: error("missing type '$type'")
    protected fun encoder(className: String): Utf8Encoder<*> =
        className2encoder[className] ?: error("missing encoder for class '$className'")
}

public fun Utf8Serializer.writeString(value: Any?): String = writeBytes(value).decodeToString(throwOnInvalidSequence = true)
public fun Utf8Serializer.readString(string: String): Any? = readBytes(string.encodeToByteArray(throwOnInvalidSequence = true))
