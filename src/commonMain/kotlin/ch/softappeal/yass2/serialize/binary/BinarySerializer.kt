package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.*
import kotlin.reflect.*

public abstract class Encoder internal constructor(internal val type: KClass<*>) {
    internal abstract fun write(writer: EncoderWriter, value: Any?)
    internal abstract fun read(reader: EncoderReader): Any?
}

internal class EncoderId(val id: Int, val encoder: Encoder)

public class BaseEncoder<T : Any>(
    type: KClass<T>, public val write: (writer: Writer, value: T) -> Unit, public val read: (reader: Reader) -> T
) : Encoder(type) {
    @Suppress("UNCHECKED_CAST")
    override fun write(writer: EncoderWriter, value: Any?) = write(writer.writer, value as T)

    override fun read(reader: EncoderReader) = read(reader.reader)
}

public class EncoderWriter(internal val writer: Writer, private val serializer: BinarySerializer) {
    public fun writeWithId(value: Any?) {
        val encoderId = when (value) {
            null -> NullEncoderId
            is List<*> -> ListEncoderId
            else -> serializer.type2encoderId[value::class] ?: error("missing type '${value::class}'")
        }
        writer.writeVarInt(encoderId.id)
        encoderId.encoder.write(this, value)
    }

    public fun writeNoIdRequired(encoderId: Int, value: Any): Unit = serializer.encoders[encoderId].write(this, value)
    public fun writeNoIdOptional(encoderId: Int, value: Any?): Unit = if (value == null) {
        writer.writeBoolean(false)
    } else {
        writer.writeBoolean(true)
        writeNoIdRequired(encoderId, value)
    }
}

public class EncoderReader(internal val reader: Reader, private val serializer: BinarySerializer) {
    public fun readWithId(): Any? = serializer.encoders[reader.readVarInt()].read(this)

    public fun readNoIdRequired(encoderId: Int): Any = serializer.encoders[encoderId].read(this)!!
    public fun readNoIdOptional(encoderId: Int): Any? = if (reader.readBoolean()) readNoIdRequired(encoderId) else null
}

private val NullEncoderId = EncoderId(0, object : Encoder(Unit::class) {
    override fun write(writer: EncoderWriter, value: Any?) {}
    override fun read(reader: EncoderReader): Any? = null
})

internal val ListEncoderId = EncoderId(1, object : Encoder(List::class) {
    override fun write(writer: EncoderWriter, value: Any?) {
        val list = value as List<*>
        writer.writer.writeVarInt(list.size)
        for (element in list) writer.writeWithId(element)
    }

    override fun read(reader: EncoderReader): MutableList<*> {
        var size = reader.reader.readVarInt()
        val list = ArrayList<Any?>(minOf(size, 100)) // prevents easy out-of-memory attack
        while (size-- > 0) list.add(reader.readWithId())
        return list
    }
})

internal const val FirstEncoderId = 2

/** Supports the following types (including optional variants): `null`, [List], [BaseEncoder] and [ClassEncoder]. */
public class BinarySerializer(encoders: List<Encoder>) : Serializer {
    internal val encoders = (listOf(NullEncoderId.encoder, ListEncoderId.encoder) + encoders).toTypedArray()
    internal val type2encoderId = HashMap<KClass<*>, EncoderId>(this.encoders.size)

    init {
        this.encoders.withIndex().forEach { (id, encoder) ->
            require(type2encoderId.put(encoder.type, EncoderId(id, encoder)) == null) {
                "duplicated type '${encoder.type}'"
            }
        }
    }

    override fun write(writer: Writer, value: Any?): Unit = EncoderWriter(writer, this).writeWithId(value)
    override fun read(reader: Reader): Any? = EncoderReader(reader, this).readWithId()
}

/**
 * The class must be concrete and must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
public class ClassEncoder<T : Any>(
    type: KClass<T>,
    private val writeProperties: (writer: EncoderWriter, instance: T) -> Unit,
    private val readInstance: (reader: EncoderReader) -> T
) : Encoder(type) {
    @Suppress("UNCHECKED_CAST")
    override fun write(writer: EncoderWriter, value: Any?) = writeProperties(writer, value as T)

    override fun read(reader: EncoderReader) = readInstance(reader)
}
