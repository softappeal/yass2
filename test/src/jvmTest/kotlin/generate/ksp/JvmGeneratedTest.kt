package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.Invocation
import ch.softappeal.yass2.SuspendInvocation
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.transport.BytesReader
import ch.softappeal.yass2.transport.BytesWriter
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals

@GenerateProxy
interface Adder {
    fun add(a: Int, b: Int): Int
}

private val AdderImpl = object : Adder {
    override fun add(a: Int, b: Int) = a + b
}

@GenerateProxy
interface SuspendAdder {
    suspend fun add(a: Int, b: Int): Int
}

private val SuspendAdderImpl = object : SuspendAdder {
    override suspend fun add(a: Int, b: Int) = a + b
}

@GenerateProxy
interface MixedAdder {
    fun add(a: Int, b: Int): Int
    suspend fun subtract(a: Int, b: Int): Int
}

private val MixedAdderImpl = object : MixedAdder {
    override fun add(a: Int, b: Int) = a + b
    override suspend fun subtract(a: Int, b: Int) = a + b
}

@GenerateBinarySerializer(
    baseEncoderClasses = [IntEncoder::class],
    enumClasses = [],
    treeConcreteClasses = [],
    graphConcreteClasses = [],
    withDumper = false,
)
private val ContractSerializer = createSerializer()

@GenerateDumper(
    treeConcreteClasses = [],
    graphConcreteClasses = [],
)
private val Dumper = createDumper {}

private fun print(function: KFunction<*>, @Suppress("UNUSED_PARAMETER") parameters: List<Any?>, invoke: Invocation): Any? {
    println(function.name)
    return invoke()
}

private suspend fun print(
    function: KFunction<*>, @Suppress("UNUSED_PARAMETER") parameters: List<Any?>, invoke: SuspendInvocation,
): Any? {
    println(function.name)
    return invoke()
}

private val AdderId = ServiceId<SuspendAdder>(1)

private fun Serializer.copy(value: Int): Int {
    val writer = BytesWriter(1000)
    write(writer, value)
    return read(BytesReader(writer.buffer)) as Int
}

class JvmGeneratedTest {
    @Test
    fun test() {
        assertEquals(1001, ContractSerializer.copy(1001))
        assertEquals("321", StringBuilder().Dumper(321).toString())
        assertEquals(3, AdderImpl.proxy(::print).add(1, 2))
        assertEquals(3, MixedAdderImpl.proxy(::print, ::print).add(1, 2))
        runBlocking {
            assertEquals(3, SuspendAdderImpl.proxy(::print).add(1, 2))
            assertEquals(3, AdderId.proxy(tunnel(AdderId.service(SuspendAdderImpl))).add(1, 2))
        }
    }
}
