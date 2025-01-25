package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import kotlin.reflect.KClass

public open class BinaryEncoder<T : Any>(
    internal val type: KClass<T>,
    private val write: Writer.(value: T) -> Unit,
    private val read: Reader.() -> T,
) {
    public fun write(writer: Writer, value: Any): Unit = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    public fun read(reader: Reader): T = reader.read()
}

/** Has built-in encoders for null and [List]. */
public abstract class BinarySerializer : Serializer {
    private data class EncoderId(val id: Int, val encoder: BinaryEncoder<*>)

    private lateinit var encoders: Array<BinaryEncoder<*>>
    private lateinit var type2encoderId: MutableMap<KClass<*>, EncoderId>

    public companion object {
        private const val NULL_ENCODER_ID = 0
        public const val LIST_ENCODER_ID: Int = 1
        public const val FIRST_ENCODER_ID: Int = 2
    }

    protected fun initialize(vararg binaryEncoders: BinaryEncoder<*>) {
        encoders = (listOf(
            BinaryEncoder(Unit::class, {}, {}), // placeholder for NULL_ENCODER_ID, methods are never called
            listEncoderId.encoder,
        ) + binaryEncoders).toTypedArray()
        type2encoderId = HashMap(encoders.size)
        encoders.forEachIndexed { id, encoder ->
            require(type2encoderId.put(encoder.type, EncoderId(id, encoder)) == null) { "duplicated type '${encoder.type}'" }
        }
    }

    private val listEncoderId = EncoderId(LIST_ENCODER_ID, BinaryEncoder(List::class,
        { list ->
            writeVarInt(list.size)
            for (element in list) writeWithId(element)
        },
        {
            var size = readVarInt()
            ArrayList<Any?>(minOf(size, 10)).apply { // prevents easy out-of-memory attack
                while (size-- > 0) add(readWithId())
            }
        }
    ))

    protected fun Writer.writeWithId(value: Any?) {
        val (id, encoder) = when (value) {
            null -> {
                writeVarInt(NULL_ENCODER_ID)
                return
            }
            is List<*> -> listEncoderId
            else -> type2encoderId[value::class] ?: error("missing type '${value::class}'")
        }
        writeVarInt(id)
        encoder.write(this, value)
    }

    protected fun Writer.writeNoIdRequired(encoderId: Int, value: Any): Unit = encoders[encoderId].write(this, value)
    protected fun Writer.writeNoIdOptional(encoderId: Int, value: Any?): Unit = if (value == null) {
        writeBinaryBoolean(false)
    } else {
        writeBinaryBoolean(true)
        writeNoIdRequired(encoderId, value)
    }

    protected fun Reader.readWithId(): Any? {
        val id = readVarInt()
        return if (id == NULL_ENCODER_ID) null else encoders[id].read(this)
    }

    protected fun Reader.readNoIdRequired(encoderId: Int): Any = encoders[encoderId].read(this)
    protected fun Reader.readNoIdOptional(encoderId: Int): Any? = if (readBinaryBoolean()) readNoIdRequired(encoderId) else null

    override fun write(writer: Writer, value: Any?): Unit = writer.writeWithId(value)
    override fun read(reader: Reader): Any? = reader.readWithId()
}
