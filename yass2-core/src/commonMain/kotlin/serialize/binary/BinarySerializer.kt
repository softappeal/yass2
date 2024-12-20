package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import kotlin.reflect.KClass

public abstract class Encoder internal constructor(internal val type: KClass<*>) {
    internal abstract fun write(writer: Writer, value: Any?)
    internal abstract fun read(reader: Reader): Any?
}

public const val NULL_ENCODER_ID: Int = 0
public const val LIST_ENCODER_ID: Int = 1
public const val FIRST_ENCODER_ID: Int = 2

public abstract class BaseEncoder<T : Any>(
    type: KClass<T>,
    public val write: Writer.(value: T) -> Unit,
    public val read: Reader.() -> T,
) : Encoder(type) {
    override fun write(writer: Writer, value: Any?) = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    override fun read(reader: Reader) = reader.read()
}

/**
 * The class must be concrete and must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
public class ClassEncoder<T : Any>(
    type: KClass<T>,
    private val writeProperties: Writer.(instance: T) -> Unit,
    private val readInstance: Reader.() -> T,
) : Encoder(type) {
    override fun write(writer: Writer, value: Any?) = writeProperties(writer, @Suppress("UNCHECKED_CAST") (value as T))
    override fun read(reader: Reader) = readInstance(reader)
}

/** Supports the following types (including optional variants): `null`, [List], [BaseEncoder] and [ClassEncoder]. */
public abstract class BinarySerializer : Serializer {
    private data class EncoderId(val id: Int, val encoder: Encoder)

    private val nullEncoderId = EncoderId(NULL_ENCODER_ID, object : Encoder(Unit::class) {
        override fun write(writer: Writer, value: Any?) {}
        override fun read(reader: Reader): Any? = null
    })

    private val listEncoderId: EncoderId = EncoderId(LIST_ENCODER_ID, object : Encoder(List::class) {
        override fun write(writer: Writer, value: Any?) {
            val list = value as List<*>
            writer.writeVarInt(list.size)
            for (element in list) writer.writeWithId(element)
        }

        override fun read(reader: Reader): MutableList<*> {
            var size = reader.readVarInt()
            return ArrayList<Any?>(minOf(size, 100)).apply { // prevents easy out-of-memory attack
                while (size-- > 0) add(reader.readWithId())
            }
        }
    })

    private lateinit var encoders: Array<Encoder>
    private lateinit var type2encoderId: MutableMap<KClass<*>, EncoderId>

    protected fun initialize(vararg encoders: Encoder) {
        this.encoders = (listOf(nullEncoderId.encoder, listEncoderId.encoder) + encoders).toTypedArray()
        type2encoderId = HashMap(this.encoders.size)
        this.encoders.forEachIndexed { id, encoder ->
            require(type2encoderId.put(encoder.type, EncoderId(id, encoder)) == null) { "duplicated type '${encoder.type}'" }
        }
    }

    protected fun Writer.writeWithId(value: Any?) {
        val (id, encoder) = when (value) {
            null -> nullEncoderId
            is List<*> -> listEncoderId
            else -> type2encoderId[value::class] ?: error("missing type '${value::class}'")
        }
        writeVarInt(id)
        encoder.write(this, value)
    }

    protected fun Writer.writeNoIdRequired(encoderId: Int, value: Any): Unit = encoders[encoderId].write(this, value)
    protected fun Writer.writeNoIdOptional(encoderId: Int, value: Any?): Unit = if (value == null) {
        writeBoolean(false)
    } else {
        writeBoolean(true)
        writeNoIdRequired(encoderId, value)
    }

    protected fun Reader.readWithId(): Any? = encoders[readVarInt()].read(this)
    protected fun Reader.readNoIdRequired(encoderId: Int): Any = encoders[encoderId].read(this)!!
    protected fun Reader.readNoIdOptional(encoderId: Int): Any? = if (readBoolean()) readNoIdRequired(encoderId) else null

    override fun write(writer: Writer, value: Any?): Unit = writer.writeWithId(value)
    override fun read(reader: Reader): Any? = reader.readWithId()
}

@Target(AnnotationTarget.PROPERTY)
public annotation class GenerateBinarySerializer(
    val baseEncoderClasses: Array<KClass<out BaseEncoder<*>>>,
    val enumClasses: Array<KClass<*>>,
    val concreteClasses: Array<KClass<*>>,
)
