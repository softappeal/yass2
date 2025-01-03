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

public fun Writer.writeTextByte(char: Char) {
    writeByte(char.code.toByte())
}

private fun Reader.readTextByte(): Char = readByte().toInt().toChar()

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

public const val TEXT_DELIMITER: Char = ','
public const val TEXT_LIST_START: Char = '['
public const val TEXT_LIST_END: Char = ']'
public const val TEXT_OBJECT_START: Char = '('
public const val TEXT_OBJECT_END: Char = ')'
public const val TEXT_NULL: Char = '*'
public const val TEXT_PROPERTY: Char = '='
public const val TEXT_STRING: Char = '"'

public const val TEXT_STRING_ENCODER_ID: Int = 0
public const val TEXT_LIST_ENCODER_ID: Int = 1
public const val TEXT_FIRST_ENCODER_ID: Int = 2

/**
 * Has built-in encoders for null, [String] and [List].
 * It reads/writes UTF-8 encoded strings.
 */
public abstract class TextSerializer : Serializer {
    // TODO: review for full/correct UTF-8 support
    // TODO: add skipping of white space; add multiline output
    // TODO: add MessageSerializer and PacketSerializer

    private lateinit var encoders: Array<TextEncoder<*>>
    private lateinit var type2encoder: MutableMap<KClass<*>, TextEncoder<*>>
    private lateinit var className2encoder: MutableMap<String, TextEncoder<*>>

    protected fun initialize(vararg encoders: TextEncoder<*>) {
        this.encoders = (listOf(stringEncoder, listEncoder) + encoders).toTypedArray()
        type2encoder = HashMap(this.encoders.size)
        className2encoder = HashMap(this.encoders.size)
        this.encoders.forEach { encoder ->
            require(type2encoder.put(encoder.type, encoder) == null) { "duplicated type '${encoder.type}'" }
            require(className2encoder.put(encoder.type.simpleName!!, encoder) == null) { "duplicated className '${encoder.type}'" }
        }
    }

    private val stringEncoder = TextEncoder(String::class, // TODO: add quoting of STRING
        { value ->
            writeTextByte(TEXT_STRING)
            writeTextBytes(value)
            writeTextByte(TEXT_STRING)
        },
        {
            readNextChar()
            readDelimiter(TEXT_STRING)
        }
    )

    private val listEncoder = TextEncoder(List::class,
        { list ->
            writeTextByte(TEXT_LIST_START)
            list.forEachIndexed { index, element ->
                if (index != 0) writeTextByte(TEXT_DELIMITER)
                writeWithId(element)
            }
            writeTextByte(TEXT_LIST_END)
        },
        {
            check(expectedChar(TEXT_LIST_START)) { "'$TEXT_LIST_START' expected" }
            ArrayList<Any?>(10).apply {
                var first = true
                while (true) {
                    readNextChar()
                    if (expectedChar(TEXT_LIST_END)) return@apply
                    if (first) first = false else if (expectedChar(TEXT_DELIMITER)) readNextChar()
                    add(readWithId())
                }
            }
        }
    )

    private fun TextWriter.writeWithId(value: Any?) {
        when (value) {
            null -> writeTextByte(TEXT_NULL)
            is String -> stringEncoder.write(this, value)
            is List<*> -> listEncoder.write(this, value)
            else -> {
                val encoder = type2encoder[value::class] ?: error("missing type '${value::class}'")
                writeTextBytes("${encoder.type.simpleName}$TEXT_OBJECT_START")
                encoder.write(this, value)
                writeTextByte(TEXT_OBJECT_END)
            }
        }
    }

    public inner class TextWriter(private val writer: Writer) : Writer by writer {
        private var first: Boolean = true
        private fun writeProperty(property: String, value: Any?, writeValue: () -> Unit) {
            if (value == null) return
            if (first) first = false else writeTextByte(TEXT_DELIMITER)
            writeTextBytes("$property$TEXT_PROPERTY")
            writeValue()
        }

        public fun writeWithId(property: String, value: Any?) {
            writeProperty(property, value) { TextWriter(this).writeWithId(value) }
        }

        public fun writeNoId(property: String, id: Int, value: Any?) {
            writeProperty(property, value) { encoders[id].write(TextWriter(this), value) }
        }
    }

    private fun TextReader.readWithId(): Any? = when {
        expectedChar(TEXT_NULL) -> null
        expectedChar(TEXT_STRING) -> stringEncoder.read(this)
        expectedChar(TEXT_LIST_START) -> listEncoder.read(this)
        else -> {
            val className = readDelimiter(TEXT_OBJECT_START)
            val encoder = className2encoder[className] ?: error("missing encoder for class '${className}'")
            readNextChar()
            if (encoder is ClassTextEncoder) readObject(encoder) else encoder.read(this)
        }
    }

    public inner class TextReader(private val reader: Reader, private var nextChar: Char) : Reader by reader {
        internal fun expectedChar(char: Char) = nextChar == char
        internal fun readNextChar() {
            nextChar = readTextByte()
        }

        private fun readUntil(predicate: () -> Boolean) = buildString {
            while (!predicate()) {
                append(nextChar)
                readNextChar()
            }
        }

        internal fun readDelimiter(delimiter: Char) = readUntil { expectedChar(delimiter) }

        /**
         * Reads until [TEXT_OBJECT_END] or [TEXT_DELIMITER].
         */
        public fun readTextBytes(): String = readUntil { expectedChar(TEXT_OBJECT_END) || expectedChar(TEXT_DELIMITER) }

        private lateinit var properties: MutableMap<String, Any?>

        internal fun readObject(encoder: ClassTextEncoder<*>): Any {
            properties = mutableMapOf()
            var first = true
            while (true) {
                if (expectedChar(TEXT_OBJECT_END)) break
                if (first) first = false else if (expectedChar(TEXT_DELIMITER)) readNextChar()
                val propertyName = readDelimiter(TEXT_PROPERTY)
                readNextChar()
                val id = encoder.property2id[propertyName]
                properties[propertyName] = if (id != null) encoders[id].read(this) else {
                    with(TextReader(this, nextChar)) { readWithId() }.apply { readNextChar() }
                }
            }
            return encoder.read(this)
        }

        public fun getProperty(property: String): Any? {
            return properties[property]
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = TextWriter(writer).writeWithId(value)
    override fun read(reader: Reader): Any? = TextReader(reader, reader.readTextByte()).readWithId()
}

public fun TextSerializer.writeString(value: Any?): String = with(BytesWriter(1000)) {
    write(this, value)
    buffer.decodeToString(0, current, throwOnInvalidSequence = true)
}

public fun TextSerializer.readString(string: String): Any? =
    with(BytesReader(string.encodeToByteArray(throwOnInvalidSequence = true))) {
        read(this).apply { check(isDrained) }
    }
