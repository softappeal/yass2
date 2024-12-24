package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import kotlin.reflect.KClass

public abstract class AbstractEncoder internal constructor(internal val type: KClass<*>) {
    internal abstract fun write(writer: Writer, value: Any?)
    internal abstract fun read(reader: Reader): Any?
}

public open class Encoder<T : Any>(
    type: KClass<T>,
    public val write: Writer.(value: T) -> Unit,
    public val read: Reader.() -> T,
) : AbstractEncoder(type) {
    override fun write(writer: Writer, value: Any?) = writer.write(@Suppress("UNCHECKED_CAST") (value as T))
    override fun read(reader: Reader) = reader.read()
}

public const val NULL_ENCODER_ID: Int = 0
public const val LIST_ENCODER_ID: Int = 1
public const val FIRST_ENCODER_ID: Int = 2

public abstract class BinarySerializer : Serializer {
    private data class EncoderId(val id: Int, val encoder: AbstractEncoder)

    private val nullEncoderId = EncoderId(NULL_ENCODER_ID, object : AbstractEncoder(Unit::class) {
        override fun write(writer: Writer, value: Any?) {}
        override fun read(reader: Reader) = null
    })

    private val listEncoderId = EncoderId(LIST_ENCODER_ID, Encoder(List::class,
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

    private lateinit var encoders: Array<AbstractEncoder>
    private lateinit var type2encoderId: MutableMap<KClass<*>, EncoderId>

    protected fun initialize(vararg encoders: Encoder<*>) {
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

/**
 * [concreteClasses] must be concrete and must have a primary constructor and all its parameters must be properties.
 * Body properties are allowed but must be of `var` kind.
 * Inheritance is supported.
 */
@Target(AnnotationTarget.PROPERTY)
public annotation class GenerateBinarySerializer(
    val baseEncoderClasses: Array<KClass<out Encoder<*>>>,
    val enumClasses: Array<KClass<*>>,
    val concreteClasses: Array<KClass<*>>,
)
