package ch.softappeal.yass2.core.serialize.string

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.Writer
import ch.softappeal.yass2.core.serialize.fromByteArray
import ch.softappeal.yass2.core.serialize.toByteArray
import kotlin.reflect.KClass

public const val QUOTE: Int = '"'.code
public const val COMMA: Int = ','.code
public const val LPAREN: Int = '('.code
public const val RPAREN: Int = ')'.code
public const val SP: Int = ' '.code
private const val BS = '\\'.code
private const val NL = '\n'.code
private const val CR = '\r'.code
private const val TAB = '\t'.code
private const val DOLLAR = '$'.code
private const val NULL = "null"
private const val TRUE = "true"
private const val FALSE = "false"

public fun Int.isWhitespace(): Boolean = this == SP || this == TAB || this == NL || this == CR

private const val S_BS = BS.toChar().toString()
private const val S_BS_BS = S_BS + S_BS
private const val S_QUOTE = QUOTE.toChar().toString()
private const val S_BS_QUOTE = S_BS + S_QUOTE
private const val S_NL = NL.toChar().toString()
private const val S_BS_NL = S_BS + "n"
private const val S_CR = CR.toChar().toString()
private const val S_BS_CR = S_BS + "r"
private const val S_TAB = TAB.toChar().toString()
private const val S_BS_TAB = S_BS + "t"
private const val S_DOLLAR = DOLLAR.toChar().toString()
private const val S_BS_DOLLAR = "$S_BS$"

private val Tab = ByteArray(4) { SP.toByte() }

public abstract class StringWriter(
    private val writer: Writer,
    protected val indent: Int,
    private val escapeDollar: Boolean = false,
) : Writer by writer {
    protected abstract fun writeList(list: List<*>)

    private fun writeStringBuiltIn(string: String) {
        writeByte(QUOTE)
        writeString(
            string
                .replace(S_BS, S_BS_BS) // must be first!
                .replace(S_QUOTE, S_BS_QUOTE)
                .replace(S_NL, S_BS_NL)
                .replace(S_CR, S_BS_CR)
                .replace(S_TAB, S_BS_TAB)
                .run { if (escapeDollar) replace(S_DOLLAR, S_BS_DOLLAR) else this }
        )
        writeByte(QUOTE)
    }

    protected fun writeBuiltIn(value: Any?): Boolean {
        when (value) {
            null -> writeString(NULL)
            false -> writeString(FALSE)
            true -> writeString(TRUE)
            is String -> writeStringBuiltIn(value)
            is List<*> -> writeList(value)
            else -> return false
        }
        return true
    }

    protected fun writePropertyBuiltIn(value: Any?, encoderId: Int): Boolean {
        @OptIn(InternalApi::class)
        when (encoderId) {
            STRING_STRING_ENCODER_ID -> writeStringBuiltIn(value as String)
            STRING_BOOLEAN_ENCODER_ID -> writeString(if (value as Boolean) TRUE else FALSE)
            STRING_LIST_ENCODER_ID -> writeList(value as List<*>)
            else -> return true
        }
        return false
    }

    public fun writeByte(asciiCodePoint: Int) {
        writeByte(asciiCodePoint.toByte())
    }

    public fun writeString(string: String) {
        writeByteArray(string.encodeToByteArray(throwOnInvalidSequence = true))
    }

    public fun writeIndent() {
        repeat(indent) { writeByteArray(Tab) }
    }

    public fun writeIndentMinus1() {
        repeat(indent - 1) { writeByteArray(Tab) }
    }

    public fun writeNewLine() {
        writeByte(NL)
    }

    public open fun startBodyProperties() {}
    public abstract fun writeProperty(name: String, value: Any?)
    public abstract fun writeProperty(name: String, value: Any?, encoderId: Int)
}

public abstract class StringReader(
    private val reader: Reader,
    private var _nextCodePoint: Int,
    private val escapeDollar: Boolean = false,
) : Reader by reader {
    public fun readStringBuiltIn(): String = buildString {
        while (true) {
            readNextCodePoint()
            when {
                expectedCodePoint(QUOTE) -> break
                expectedCodePoint(BS) -> {
                    readNextCodePoint()
                    when {
                        expectedCodePoint(QUOTE) -> append(QUOTE.toChar())
                        expectedCodePoint(BS) -> append(BS.toChar())
                        expectedCodePoint('n'.code) -> append(NL.toChar())
                        expectedCodePoint('r'.code) -> append(CR.toChar())
                        expectedCodePoint('t'.code) -> append(TAB.toChar())
                        escapeDollar && expectedCodePoint(DOLLAR) -> append(DOLLAR.toChar())
                        else -> error("illegal escape with codePoint $nextCodePoint")
                    }
                }
                else -> addCodePoint(nextCodePoint)
            }
        }
    }

    public val nextCodePoint: Int get() = _nextCodePoint
    public fun expectedCodePoint(codePoint: Int): Boolean = _nextCodePoint == codePoint
    public fun readNextCodePoint() {
        _nextCodePoint = readCodePoint()
    }

    public fun checkExpectedCodePoint(codePoint: Int) {
        check(expectedCodePoint(codePoint)) { "'${codePoint.toChar()}' expected instead of '${nextCodePoint.toChar()}'" }
    }

    private fun isWhitespace() = nextCodePoint.isWhitespace()

    public fun skipWhitespace() {
        while (isWhitespace()) readNextCodePoint()
    }

    public fun readNextCodePointAndSkipWhitespace() {
        readNextCodePoint()
        skipWhitespace()
    }

    /** @see BaseStringEncoder */
    public fun readBaseString(): String =
        readUntil { expectedCodePoint(QUOTE) || expectedCodePoint(COMMA) || expectedCodePoint(RPAREN) }

    public fun readUntil(isEnd: () -> Boolean): String = buildString {
        while (!isEnd() && !isWhitespace()) {
            addCodePoint(nextCodePoint)
            readNextCodePoint()
        }
        skipWhitespace()
    }

    public data class ReadUntilBuiltInResult(val handled: Boolean, val result: Boolean?, val className: String)

    public fun readUntilBuiltIn(isEnd: () -> Boolean): ReadUntilBuiltInResult {
        val className = buildString {
            while (!isEnd() && !isWhitespace()) {
                addCodePoint(nextCodePoint)
                val head = toString()
                if (NULL == head || FALSE == head || TRUE == head) return@buildString
                readNextCodePoint()
            }
            skipWhitespace()
        }
        @Suppress("BooleanLiteralArgument")
        return when (className) {
            NULL -> ReadUntilBuiltInResult(true, null, className)
            FALSE -> ReadUntilBuiltInResult(true, false, className)
            TRUE -> ReadUntilBuiltInResult(true, true, className)
            else -> ReadUntilBuiltInResult(false, null, className)
        }
    }

    private val propertyNameToValue = mutableMapOf<String, Any?>()

    protected fun ClassStringEncoder<*>.addProperty(name: String, value: Any?) {
        check(!propertyNameToValue.containsKey(name)) { "duplicated property '${type.simpleName}.$name'" }
        propertyNameToValue[name] = value
    }

    public fun getProperty(name: String): Any? = propertyNameToValue[name]

    protected fun ClassStringEncoder<*>.checkMissingProperties() {
        val missingProperties = propertyToEncoderId.keys - propertyNameToValue.keys
        check(missingProperties.isEmpty()) { "missing properties '${missingProperties.sorted()}' for '${type.simpleName}'" }
    }
}

public open class StringEncoder<T : Any>(
    public val type: KClass<T>,
    private val write: StringWriter.(value: T) -> Unit,
    private val read: StringReader.() -> T,
) {
    public fun write(writer: StringWriter, value: Any?): Unit = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    public fun read(reader: StringReader): T = reader.read()
}

public abstract class BaseStringEncoder<T : Any>(
    type: KClass<T>,
    /**
     * Result string must not contain whitespace, `"`, `,` or `)`.
     * @see StringReader.readBaseString
     */
    public val write: (value: T) -> String,
    private val read: String.() -> T,
) : StringEncoder<T>(
    type,
    { value ->
        writeString(write(value).apply {
            check(indexOfFirst { it.code.isWhitespace() || it.code == QUOTE || it.code == COMMA || it.code == RPAREN } < 0) {
                "'$this' must not contain whitespace, '${QUOTE.toChar()}', '${COMMA.toChar()}' or '${RPAREN.toChar()}'"
            }
        })
    },
    { readBaseString().read() }
) {
    public fun read(string: String): T = string.read()
}

public class ClassStringEncoder<T : Any>(
    type: KClass<T>,
    public val hasBodyProperties: Boolean,
    write: StringWriter.(value: T) -> Unit,
    read: StringReader.() -> T,
    /** see [STRING_NO_ENCODER_ID] */
    vararg propertyEncoderIds: Pair<String, Int>,
) : StringEncoder<T>(type, write, read) {
    internal val propertyToEncoderId = propertyEncoderIds.toMap()
    public fun encoderId(property: String): Int {
        val encoderId = propertyToEncoderId[property]
        check(encoderId != null) { "no property '${type.simpleName}.$property'" }
        return encoderId
    }
}

/**
 * It reads/writes UTF-8 encoded strings.
 * It has built-in encoders for `null`, [String], [Boolean] and [List].
 */
public abstract class StringSerializer(stringEncoders: List<StringEncoder<*>>) : Serializer {
    private val encoders = (listOf(
        // placeholders, methods are never called
        StringEncoder(String::class, {}, { "" }),
        StringEncoder(Boolean::class, {}, { false }),
        StringEncoder(List::class, {}, { listOf<Int>() }),
    ) + stringEncoders).toTypedArray()
    private val typeToEncoder = HashMap<KClass<*>, StringEncoder<*>>(encoders.size)
    private val classNameToEncoder = HashMap<String, StringEncoder<*>>(encoders.size)

    init {
        encoders.forEach { encoder ->
            require(typeToEncoder.put(encoder.type, encoder) == null) { "duplicated type '${encoder.type}'" }
            require(classNameToEncoder.put(encoder.type.simpleName!!, encoder) == null) {
                "duplicated className '${encoder.type.simpleName}'"
            }
        }
    }

    protected fun encoder(encoderId: Int): StringEncoder<*> = encoders[encoderId]
    protected fun encoder(type: KClass<*>): StringEncoder<*> = typeToEncoder[type] ?: error("missing type '$type'")
    protected fun encoder(className: String): StringEncoder<*> =
        classNameToEncoder[className] ?: error("missing encoder for class '$className'")
}

@InternalApi public const val STRING_NO_ENCODER_ID: Int = -1
@InternalApi public const val STRING_STRING_ENCODER_ID: Int = 0
@InternalApi public const val STRING_BOOLEAN_ENCODER_ID: Int = 1
@InternalApi public const val STRING_LIST_ENCODER_ID: Int = 2
@InternalApi public const val STRING_FIRST_ENCODER_ID: Int = 3

public fun StringSerializer.toString(value: Any?): String = toByteArray(value).decodeToString(throwOnInvalidSequence = true)
public fun StringSerializer.fromString(string: String): Any? =
    fromByteArray(string.encodeToByteArray(throwOnInvalidSequence = true))

public annotation class StringEncoderObjects(vararg val value: KClass<out BaseStringEncoder<*>>)
