package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.InternalApi
import ch.softappeal.yass2.serialize.Property
import ch.softappeal.yass2.serialize.Reader
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.Writer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

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

    /** See [NULL_ENCODER_ID] and [LIST_ENCODER_ID]. */
    protected fun initialize(vararg binaryEncoders: BinaryEncoder<*>) {
        encoders = (listOf(
            BinaryEncoder(Unit::class, {}, {}), // placeholder for NULL_ENCODER_ID, methods are never called
            listEncoderId.encoder,
        ) + binaryEncoders).toTypedArray()
        type2encoderId = HashMap(encoders.size)
        encoders.forEachIndexed { encoderId, encoder ->
            require(type2encoderId.put(encoder.type, EncoderId(encoderId, encoder)) == null) { "duplicated type '${encoder.type}'" }
        }
    }

    private val listEncoderId = EncoderId(@OptIn(InternalApi::class) LIST_ENCODER_ID, BinaryEncoder(List::class,
        { list ->
            writeVarInt(list.size)
            for (element in list) writeObject(element)
        },
        {
            var size = readVarInt()
            ArrayList<Any?>(minOf(size, 10)).apply { // prevents easy out-of-memory attack
                while (size-- > 0) add(readObject())
            }
        }
    ))

    protected fun Writer.writeObject(value: Any?) {
        val (encoderId, encoder) = when (value) {
            null -> {
                writeVarInt(NULL_ENCODER_ID)
                return
            }
            is List<*> -> listEncoderId
            else -> type2encoderId[value::class] ?: error("missing type '${value::class}'")
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
        return if (encoderId == NULL_ENCODER_ID) null else encoders[encoderId].read(this)
    }

    protected fun Reader.readRequired(encoderId: Int): Any = encoders[encoderId].read(this)
    protected fun Reader.readOptional(encoderId: Int): Any? = if (readBinaryBoolean()) readRequired(encoderId) else null

    override fun write(writer: Writer, value: Any?): Unit = writer.writeObject(value)
    override fun read(reader: Reader): Any? = reader.readObject()
}

private const val NO_ENCODER_ID = -1
private const val NULL_ENCODER_ID = 0
@InternalApi public const val LIST_ENCODER_ID: Int = 1
@InternalApi public const val FIRST_ENCODER_ID: Int = 2

@InternalApi
public class BinaryProperty(
    baseClasses: List<KClass<*>>,
    concreteClasses: List<KClass<*>>,
    property: KProperty1<out Any, *>,
    type: KClass<*>,
    private val nullable: Boolean,
    hasSuperClass: KClass<*>.(superClass: KClass<*>) -> Boolean,
) : Property(property) {
    private val encoderId = if (type == List::class) LIST_ENCODER_ID else {
        val baseClassIndex = baseClasses.indexOfFirst { it == type }
        if (baseClassIndex >= 0) baseClassIndex + FIRST_ENCODER_ID else {
            val concreteClassIndex = concreteClasses.indexOfFirst { it == type }
            if (
                concreteClassIndex >= 0 && concreteClasses.none { it.hasSuperClass(concreteClasses[concreteClassIndex]) }
            ) concreteClassIndex + baseClasses.size + FIRST_ENCODER_ID else NO_ENCODER_ID
        }
    }

    private fun suffix() = if (encoderId == NO_ENCODER_ID) "Object" else if (nullable) "Optional" else "Required"

    public fun writeObject(value: String): String =
        "write${suffix()}($value${if (encoderId == NO_ENCODER_ID) "" else ", $encoderId"})"

    public fun readObject(): String = "read${suffix()}(${if (encoderId == NO_ENCODER_ID) "" else encoderId})"

    public fun meta(): String =
        if (encoderId == NO_ENCODER_ID) "object" else "${if (nullable) "optional" else "required"} $encoderId"
}
