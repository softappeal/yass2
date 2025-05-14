package ch.softappeal.yass2.core.serialize.binary

import ch.softappeal.yass2.core.InternalApi
import ch.softappeal.yass2.core.serialize.Reader
import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.Writer
import kotlin.reflect.KClass

public open class BinaryEncoder<T : Any>(
    internal val type: KClass<T>,
    private val write: Writer.(value: T) -> Unit,
    private val read: Reader.() -> T,
) {
    public fun write(writer: Writer, value: Any): Unit = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    public fun read(reader: Reader): T = reader.read()
}

/** Has built-in encoders for `null` and [List]. */
public abstract class BinarySerializer : Serializer {
    private data class EncoderId(val id: Int, val encoder: BinaryEncoder<*>)

    private lateinit var encoders: Array<BinaryEncoder<*>>
    private lateinit var typeToEncoderId: Map<KClass<*>, EncoderId>

    /** See [BINARY_NULL_ENCODER_ID] and [BINARY_LIST_ENCODER_ID]. */
    protected fun initialize(vararg binaryEncoders: BinaryEncoder<*>) {
        encoders = (listOf(
            BinaryEncoder(Unit::class, {}, {}), // placeholder for NULL_ENCODER_ID, methods are never called
            listEncoderId.encoder,
        ) + binaryEncoders).toTypedArray()
        typeToEncoderId = buildMap {
            encoders.forEachIndexed { encoderId, encoder ->
                require(put(encoder.type, EncoderId(encoderId, encoder)) == null) { "duplicated type '${encoder.type}'" }
            }
        }
    }

    private val listEncoderId = EncoderId(
        @OptIn(InternalApi::class) BINARY_LIST_ENCODER_ID,
        BinaryEncoder(
            List::class,
            { list ->
                writeVarInt(list.size)
                for (element in list) writeObject(element)
            },
            {
                var size = readVarInt()
                ArrayList<Any?>(minOf(size, 100)).apply { // prevents easy out-of-memory attack
                    while (size-- > 0) add(readObject())
                }
            }
        )
    )

    protected fun Writer.writeObject(value: Any?) {
        val (encoderId, encoder) = when (value) {
            null -> {
                writeVarInt(BINARY_NULL_ENCODER_ID)
                return
            }
            is List<*> -> listEncoderId
            else -> typeToEncoderId[value::class] ?: error("missing type '${value::class}'")
        }
        writeVarInt(encoderId)
        encoder.write(this, value)
    }

    protected fun Writer.writeRequired(value: Any, encoderId: Int): Unit = encoders[encoderId].write(this, value)
    protected fun Writer.writeOptional(value: Any?, encoderId: Int): Unit = if (value == null) {
        writeBinaryBoolean(false)
    } else {
        writeBinaryBoolean(true)
        writeRequired(value, encoderId)
    }

    protected fun Reader.readObject(): Any? {
        val encoderId = readVarInt()
        return if (encoderId == BINARY_NULL_ENCODER_ID) null else encoders[encoderId].read(this)
    }

    protected fun Reader.readRequired(encoderId: Int): Any = encoders[encoderId].read(this)
    protected fun Reader.readOptional(encoderId: Int): Any? = if (readBinaryBoolean()) readRequired(encoderId) else null

    override fun write(writer: Writer, value: Any?): Unit = writer.writeObject(value)
    override fun read(reader: Reader): Any? = reader.readObject()
}

@InternalApi public const val BINARY_NO_ENCODER_ID: Int = -1
private const val BINARY_NULL_ENCODER_ID: Int = 0
@InternalApi public const val BINARY_LIST_ENCODER_ID: Int = 1
@InternalApi public const val BINARY_FIRST_ENCODER_ID: Int = 2

public annotation class BinaryEncoderObjects(vararg val value: KClass<out BinaryEncoder<*>>)
