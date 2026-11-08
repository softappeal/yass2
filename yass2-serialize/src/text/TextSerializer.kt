package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.serialize.InternalSerializeApi
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import ch.softappeal.yass2.serialize.fromByteArray
import ch.softappeal.yass2.serialize.toByteArray
import kotlin.reflect.KClass

private const val QUOTE = '"'
private const val LPAREN = '('
private const val RPAREN = ')'
private const val COLON = ':'
private const val LBRACKET = '['
private const val RBRACKET = ']'
private const val SP = ' '
private const val BS = '\\'
private const val NL = '\n'
private const val NULL = "null"
private const val TRUE = "true"
private const val FALSE = "false"

private val Tab = ByteArray(4) { SP.code.toByte() }

public open class AbstractTextEncoder<T : Any> internal constructor(
    internal val type: KClass<T>,
    internal val write: TextSerializer.TextWriter.(value: T) -> Unit,
    internal val read: TextSerializer.TextReader.() -> T,
)

public abstract class TextEncoder<T : Any>(
    type: KClass<T>,
    /** @return NOTE: must not contain whitespace or `)`. */
    internal val writeBase: T.() -> String,
    internal val readBase: String.() -> T,
) : AbstractTextEncoder<T>(
    type,
    { value ->
        writeString(writeBase(value).apply {
            check(indexOfFirst { it.isWhitespace() || it == RPAREN } < 0) {
                "'$this' must not contain whitespace or '$RPAREN'"
            }
        })
    },
    {
        readString(RPAREN).readBase()
    },
)

/** NOTE: nullable properties can't be set explicitly to null; they are null if omitted */
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

        private fun writeAsciiChar(asciiChar: Char) {
            writeByte(asciiChar.code.toByte())
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
                writeString(buildString {
                    string.forEach { char ->
                        if (char != QUOTE && char != BS) append(char) else {
                            append(BS)
                            append(if (char == QUOTE) QUOTE else BS)
                        }
                    }
                })
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
        private fun expectedAsciiChar(asciiChar: Char) = nextCodePoint.toChar() == asciiChar

        private fun readNextCodePoint() {
            nextCodePoint = readCodePoint()
        }

        private fun isWhitespace() = nextCodePoint.toChar().isWhitespace()

        private fun skipWhitespace() {
            while (isWhitespace()) readNextCodePoint()
        }

        private fun readNextCodePointAndSkipWhitespace() {
            readNextCodePoint()
            skipWhitespace()
        }

        internal fun readString(untilAsciiChar: Char) = buildString {
            while (!expectedAsciiChar(untilAsciiChar) && !isWhitespace()) {
                addCodePoint(nextCodePoint)
                readNextCodePoint()
            }
            skipWhitespace()
        }

        private val propertyNameToValue = mutableMapOf<String, Any>()

        public fun getProperty(name: String): Any? = propertyNameToValue[name]

        private fun ClassTextEncoder<*>.addProperty(name: String, value: Any) {
            check(!propertyNameToValue.containsKey(name)) { "duplicated property '${type.simpleName}.$name'" }
            propertyNameToValue[name] = value
        }

        internal fun readObject(): Any? {

            fun readQuotedString() = buildString {
                while (true) {
                    readNextCodePoint()
                    when {
                        expectedAsciiChar(QUOTE) -> break
                        expectedAsciiChar(BS) -> {
                            readNextCodePoint()
                            when {
                                expectedAsciiChar(QUOTE) -> append(QUOTE)
                                expectedAsciiChar(BS) -> append(BS)
                                else -> error("illegal escape with codePoint $nextCodePoint")
                            }
                        }
                        else -> addCodePoint(nextCodePoint)
                    }
                }
            }

            fun readList() = buildList {
                readNextCodePointAndSkipWhitespace()
                while (!expectedAsciiChar(RBRACKET)) {
                    add(readObject())
                    readNextCodePointAndSkipWhitespace()
                }
            }

            fun readClass(classEncoder: ClassTextEncoder<*>): Any {
                while (!expectedAsciiChar(RPAREN)) {
                    val propertyName = readString(COLON)
                    readNextCodePointAndSkipWhitespace()
                    val propertyEncoderId = classEncoder.propertyEncoderId(propertyName)
                    val propertyValue = if (propertyEncoderId != NO_ENCODER_ID) encoder(propertyEncoderId).read(this) else {
                        TextReader(this, nextCodePoint).readObject().apply { readNextCodePoint() }!!
                    }
                    classEncoder.addProperty(propertyName, propertyValue)
                    skipWhitespace()
                }
                return classEncoder.read(this)
            }

            skipWhitespace()
            if (expectedAsciiChar(QUOTE)) return readQuotedString()
            if (expectedAsciiChar(LBRACKET)) return readList()
            val className = buildString {
                while (!expectedAsciiChar(LPAREN) && !isWhitespace()) {
                    addCodePoint(nextCodePoint)
                    when (toString()) {
                        NULL -> return null
                        FALSE -> return false
                        TRUE -> return true
                    }
                    readNextCodePoint()
                }
            }
            skipWhitespace()
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
