package ch.softappeal.yass2.serialize

import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

public interface Writer {
    public fun writeByte(byte: Byte)
    public fun writeBytes(bytes: ByteArray)
}

public interface Reader {
    public fun readByte(): Byte
    public fun readBytes(length: Int): ByteArray
}

public interface Serializer {
    public fun write(writer: Writer, value: Any?)
    public fun read(reader: Reader): Any?
}

public fun Serializer.writeBytes(value: Any?): ByteArray = with(BytesWriter(1000)) {
    write(this, value)
    toyByteArray()
}

public fun Serializer.readBytes(byteArray: ByteArray): Any? = with(BytesReader(byteArray)) {
    read(this).apply { checkDrained() }
}

public abstract class Property(private val property: KProperty1<out Any, *>, public val returnType: KType) {
    public val name: String get() = property.name
    public val mutable: Boolean get() = property is KMutableProperty1<out Any, *>
    public val nullable: Boolean get() = returnType.isMarkedNullable
    public val classifier: KClassifier? get() = returnType.classifier
}
