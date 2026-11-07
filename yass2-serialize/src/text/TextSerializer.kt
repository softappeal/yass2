package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.serialize.InternalSerializeApi
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.fromByteArray
import ch.softappeal.yass2.serialize.toByteArray
import kotlin.reflect.KClass

private const val QUOTE = '"'.code
private const val LPAREN = '('.code
private const val RPAREN = ')'.code
private const val COLON = ':'.code
private const val LBRACKET = '['.code
private const val RBRACKET = ']'.code
private const val SP = ' '.code
private const val BS = '\\'.code
private const val NL = '\n'.code
private const val CR = '\r'.code
private const val TAB = '\t'.code
private const val NULL = "null"
private const val TRUE = "true"
private const val FALSE = "false"

private fun Int.isWhitespace() = this == SP || this == TAB || this == NL || this == CR

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

private val Tab = ByteArray(4) { SP.toByte() }

public open class AbstractTextEncoder<T : Any> internal constructor(
    internal val type: KClass<T>,
    internal val write: TextSerializer.TextWriter.(value: T) -> Unit,
    internal val read: TextSerializer.TextReader.() -> T,
)

public abstract class TextEncoder<T : Any>(
    type: KClass<T>,
    /** NOTE: Result string must not contain whitespace or `)`. */
    internal val writeBase: T.() -> String,
    internal val readBase: String.() -> T,
) : AbstractTextEncoder<T>(
    type,
    { value ->
        writeString(writeBase(value).apply {
            check(indexOfFirst { it.code.isWhitespace() || it.code == RPAREN } < 0) {
                "'$this' must not contain whitespace or '${RPAREN.toChar()}'"
            }
        })
    },
    {
        readString(RPAREN).readBase()
    },
)

public class ClassTextEncoder<T : Any>(
    type: KClass<T>,
    write: TextSerializer.TextWriter.(value: T) -> Unit,
    read: TextSerializer.TextReader.() -> T,
    vararg propertyEncoderIds: Pair<String, Int>,
) : AbstractTextEncoder<T>(type, write, read) {
    private val propertyToEncoderId = propertyEncoderIds.toMap()
    internal fun propertyEncoderId(property: String): Int {
        val encoderId = propertyToEncoderId[property]
        check(encoderId != null) { "no property '${type.simpleName}.$property'" }
        return encoderId
    }
}

/**
 * It reads/writes UTF-8 encoded text.
 * It has built-in encoders for `null`, [String], [Boolean] and [List].
 */
public class TextSerializer(vararg textEncoders: AbstractTextEncoder<*>) : Serializer {

    /** @suppress */
    @InternalSerializeApi public companion object {
        public const val NO_ENCODER_ID: Int = -1
        public const val STRING_ENCODER_ID: Int = 0
        public const val BOOLEAN_ENCODER_ID: Int = 1
        public const val LIST_ENCODER_ID: Int = 2
        public const val FIRST_ENCODER_ID: Int = 3
    }

    // placeholders, methods are never called
    private fun baseEncoders() = listOf(
        AbstractTextEncoder(String::class, {}, { "" }),
        AbstractTextEncoder(Boolean::class, {}, { false }),
        AbstractTextEncoder(List::class, {}, { listOf<Int>() }),
    )

    private val encoders = (baseEncoders() + textEncoders).toTypedArray()
    private val typeToEncoder = HashMap<KClass<*>, AbstractTextEncoder<*>>(encoders.size)
    private val classNameToEncoder = HashMap<String, AbstractTextEncoder<*>>(encoders.size)

    init {
        encoders.forEach { encoder ->
            require(typeToEncoder.put(encoder.type, encoder) == null) { "duplicated type '${encoder.type}'" }
            require(classNameToEncoder.put(encoder.type.simpleName!!, encoder) == null) {
                "duplicated className '${encoder.type.simpleName}'"
            }
        }
    }

    private fun encoder(encoderId: Int) = encoders[encoderId]
    private fun encoder(type: KClass<*>) = typeToEncoder[type] ?: error("missing type '$type'")
    private fun encoder(className: String) = classNameToEncoder[className] ?: error("missing encoder for class '$className'")

    public inner class TextWriter internal constructor(
        private val writer: Writer,
        private val indent: Int,
    ) : Writer by writer {
        private fun writeIndent() {
            repeat(indent) { writeByteArray(Tab) }
        }

        private fun writeAsciiChar(asciiCodePoint: Int) {
            writeByte(asciiCodePoint.toByte())
        }

        private fun writeNewLine() {
            writeAsciiChar(NL)
        }

        internal fun writeString(string: String) {
            writeByteArray(string.encodeToByteArray(throwOnInvalidSequence = true))
        }

        private fun nested() = TextWriter(this, indent + 1)

        /** @return [value] was handled as a built-in type */
        private fun writeBuiltIn(value: Any?): Boolean {

            fun writeQuotedString(string: String) {
                writeAsciiChar(QUOTE)
                writeString(
                    string
                        .replace(S_BS, S_BS_BS) // must be first!
                        .replace(S_QUOTE, S_BS_QUOTE)
                        .replace(S_NL, S_BS_NL)
                        .replace(S_CR, S_BS_CR)
                        .replace(S_TAB, S_BS_TAB)
                )
                writeAsciiChar(QUOTE)
            }

            fun writeList(list: List<*>) {
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

            when (value) {
                null -> writeString(NULL)
                false -> writeString(FALSE)
                true -> writeString(TRUE)
                is String -> writeQuotedString(value)
                is List<*> -> writeList(value)
                else -> return false
            }
            return true
        }

        private fun write(encoder: AbstractTextEncoder<*>, value: Any) =
            @Suppress("UNCHECKED_CAST") (encoder as AbstractTextEncoder<Any>).write(this, value)

        internal fun writeObject(value: Any?) {
            if (writeBuiltIn(value)) return
            val encoder = encoder(value!!::class)
            writeString(encoder.type.simpleName!!)
            writeAsciiChar(LPAREN)
            if (encoder !is ClassTextEncoder) write(encoder, value) else {
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

        public fun writeProperty(name: String, value: Any?) {
            writeProperty(name, value) { writeObject(value) }
        }

        public fun writeProperty(name: String, value: Any?, encoderId: Int) {
            writeProperty(name, value) { if (!writeBuiltIn(value)) write(encoder(encoderId), value!!) }
        }
    }

    public inner class TextReader internal constructor(
        private val reader: Reader,
        private var nextCodePoint: Int,
    ) : Reader by reader {
        private fun expectedCodePoint(codePoint: Int) = nextCodePoint == codePoint

        private fun readNextCodePoint() {
            nextCodePoint = readCodePoint()
        }

        private fun isWhitespace() = nextCodePoint.isWhitespace()

        private fun skipWhitespace() {
            while (isWhitespace()) readNextCodePoint()
        }

        private fun readNextCodePointAndSkipWhitespace() {
            readNextCodePoint()
            skipWhitespace()
        }

        internal fun readString(until: Int) = buildString {
            while (!expectedCodePoint(until) && !isWhitespace()) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
            skipWhitespace()
        }

        private val propertyNameToValue = mutableMapOf<String, Any?>()

        public fun getProperty(name: String): Any? = propertyNameToValue[name]

        private fun ClassTextEncoder<*>.addProperty(name: String, value: Any?) {
            check(!propertyNameToValue.containsKey(name)) { "duplicated property '${type.simpleName}.$name'" }
            propertyNameToValue[name] = value
        }

        internal fun readObject(): Any? {

            fun readQuotedString() = buildString {
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
                                else -> error("illegal escape with codePoint $nextCodePoint")
                            }
                        }
                        else -> addCodePoint(nextCodePoint)
                    }
                }
            }

            fun readList() = buildList {
                readNextCodePointAndSkipWhitespace()
                while (!expectedCodePoint(RBRACKET)) {
                    add(readObject())
                    readNextCodePointAndSkipWhitespace()
                }
            }

            fun readClass(classEncoder: ClassTextEncoder<*>): Any {
                while (!expectedCodePoint(RPAREN)) {
                    val propertyName = readString(COLON)
                    readNextCodePointAndSkipWhitespace()
                    val propertyEncoderId = classEncoder.propertyEncoderId(propertyName)
                    val propertyValue = if (propertyEncoderId != NO_ENCODER_ID) encoder(propertyEncoderId).read(this) else {
                        TextReader(this, nextCodePoint).readObject().apply { readNextCodePoint() }
                    }
                    skipWhitespace()
                    classEncoder.addProperty(propertyName, propertyValue)
                }
                return classEncoder.read(this)
            }

            skipWhitespace()
            if (expectedCodePoint(QUOTE)) return readQuotedString()
            if (expectedCodePoint(LBRACKET)) return readList()

            val className = buildString {
                while (!expectedCodePoint(LPAREN) && !isWhitespace()) {
                    addCodePoint(nextCodePoint)
                    when (toString()) {
                        NULL -> return null
                        FALSE -> return false
                        TRUE -> return true
                    }
                    readNextCodePoint()
                }
                skipWhitespace()
            }

            readNextCodePointAndSkipWhitespace()
            val encoder = encoder(className)
            return if (encoder is ClassTextEncoder) readClass(encoder) else encoder.read(this)
        }
    }

    override fun Writer.write(value: Any?): Unit = TextWriter(this, 0).writeObject(value)
    override fun Reader.read(): Any? = TextReader(this, readCodePoint()).readObject()
}

public fun TextSerializer.toString(value: Any?): String = toByteArray(value).decodeToString(throwOnInvalidSequence = true)
public fun TextSerializer.fromString(string: String): Any? = fromByteArray(string.encodeToByteArray(throwOnInvalidSequence = true))
