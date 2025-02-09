package ch.softappeal.yass2.serialize.utf8

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.serialize.Property
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.readBytes
import ch.softappeal.yass2.serialize.writeBytes
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

public const val QUOTE: Byte = '"'.code.toByte()
public const val LBRACKET: Byte = '['.code.toByte()
public const val COMMA: Byte = ','.code.toByte()
public const val SP: Byte = ' '.code.toByte()
public const val COLON: Byte = ':'.code.toByte()
private const val BS = '\\'.code.toByte()
private const val NL = '\n'.code.toByte()
private const val CR = '\r'.code.toByte()
private const val TAB = '\t'.code.toByte()
private const val RBRACKET = ']'.code.toByte()

private const val S_BS = BS.toInt().toChar().toString()
private const val S_BS_BS = S_BS + S_BS
private const val S_QUOTE = QUOTE.toInt().toChar().toString()
private const val S_BS_QUOTE = S_BS + S_QUOTE
private const val S_NL = NL.toInt().toChar().toString()
private const val S_BS_NL = S_BS + "n"
private const val S_CR = CR.toInt().toChar().toString()
private const val S_BS_CR = S_BS + "r"
private const val S_TAB = TAB.toInt().toChar().toString()
private const val S_BS_TAB = S_BS + "t"

private val Tab = ByteArray(4) { SP }

public open class Utf8Encoder<T : Any>(
    public val type: KClass<T>,
    private val write: Utf8Serializer.Utf8Writer.(value: T) -> Unit,
    private val read: Utf8Serializer.Utf8Reader.() -> T,
) {
    public fun write(writer: Utf8Serializer.Utf8Writer, value: Any?): Unit = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    public fun read(reader: Utf8Serializer.Utf8Reader): T = reader.read()
}

public class ClassUtf8Encoder<T : Any>(
    type: KClass<T>,
    write: Utf8Serializer.Utf8Writer.(value: T) -> Unit,
    read: Utf8Serializer.Utf8Reader.() -> T,
    /** see [NO_ENCODER_ID] */
    vararg propertyEncoderIds: Pair<String, Int>,
) : Utf8Encoder<T>(type, write, read) {
    private val property2encoderId = propertyEncoderIds.toMap()
    public fun encoderId(property: String): Int {
        val encoderId = property2encoderId[property]
        check(encoderId != null) { "no property '${type.simpleName}.$property'" }
        return encoderId
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

        public abstract fun writeProperty(name: String, value: Any?)
        public abstract fun writeProperty(name: String, value: Any?, encoderId: Int)
        public abstract fun writeObject(value: Any?)
    }

    public abstract class Utf8Reader(private val reader: Reader, private var _nextCodePoint: Int) : Reader by reader {
        public val nextCodePoint: Int get() = _nextCodePoint
        public fun expectedCodePoint(codePoint: Byte): Boolean = _nextCodePoint == codePoint.toInt()
        public fun readNextCodePoint() {
            _nextCodePoint = readCodePoint()
        }

        protected fun isWhitespace(): Boolean =
            expectedCodePoint(SP) || expectedCodePoint(TAB) || expectedCodePoint(NL) || expectedCodePoint(CR)

        protected fun skipWhitespace() {
            while (isWhitespace()) readNextCodePoint()
        }

        public fun readNextCodePointAndSkipWhitespace() {
            readNextCodePoint()
            skipWhitespace()
        }

        public abstract fun readString(): String
        public abstract fun readObject(): Any?

        protected lateinit var properties: MutableMap<String, Any>

        protected fun Utf8Encoder<*>.addProperty(name: String, value: Any?) {
            check(value != null) { "property '${type.simpleName}.$name' must not be explicitly set to null" }
            check(properties.put(name, value) == null) { "duplicated property '${type.simpleName}.$name'" }
        }

        public fun getProperty(property: String): Any? = properties[property]
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
                    writeObject(element)
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
                    add(readObject())
                    readNextCodePointAndSkipWhitespace()
                    if (!strictListComma && expectedCodePoint(COMMA)) readNextCodePointAndSkipWhitespace()
                }
            }
        }
    )

    /** See [STRING_ENCODER_ID] and [LIST_ENCODER_ID]. */
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

    protected fun encoder(encoderId: Int): Utf8Encoder<*> = encoders[encoderId]
    protected fun encoder(type: KClass<*>): Utf8Encoder<*> = type2encoder[type] ?: error("missing type '$type'")
    protected fun encoder(className: String): Utf8Encoder<*> =
        className2encoder[className] ?: error("missing encoder for class '$className'")
}

public const val NO_ENCODER_ID: Int = -1
public const val STRING_ENCODER_ID: Int = 0
public const val LIST_ENCODER_ID: Int = 1
public const val FIRST_ENCODER_ID: Int = 2

@InternalApi
public class Utf8Property(
    property: KProperty1<out Any, *>,
    returnType: KType,
    baseClasses: List<KClass<*>>,
) : Property(property, returnType) {
    private val encoderId = when (classifier) {
        String::class -> STRING_ENCODER_ID
        List::class -> LIST_ENCODER_ID
        else -> {
            val baseClassIndex = baseClasses.indexOfFirst { it == classifier }
            if (baseClassIndex >= 0) baseClassIndex + FIRST_ENCODER_ID else NO_ENCODER_ID
        }
    }

    public fun writeProperty(reference: String): String =
        "writeProperty(\"$name\", $reference${if (encoderId == NO_ENCODER_ID) "" else ", $encoderId"})"

    public fun propertyEncoderId(): String = "\"$name\" to ${
        if (encoderId != NO_ENCODER_ID && encoderId != STRING_ENCODER_ID && encoderId != LIST_ENCODER_ID) encoderId else NO_ENCODER_ID
    }"

    public fun meta(): String = if (encoderId == NO_ENCODER_ID) "object" else encoderId.toString()
}

public fun Utf8Serializer.writeString(value: Any?): String = writeBytes(value).decodeToString(throwOnInvalidSequence = true)
public fun Utf8Serializer.readString(string: String): Any? = readBytes(string.encodeToByteArray(throwOnInvalidSequence = true))
