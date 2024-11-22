package ch.softappeal.yass2.generate

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.Interceptor
import ch.softappeal.yass2.Invocation
import ch.softappeal.yass2.SuspendInterceptor
import ch.softappeal.yass2.SuspendInvocation
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.remote.tunnel
import ch.softappeal.yass2.serialize.Serializer
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.transport.BytesReader
import ch.softappeal.yass2.transport.BytesWriter
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: shows how to use KSP generate in none-platform code
//       https://slack-chats.kotlinlang.org/t/16366233/i-m-trying-out-kotlin-2-0-beta-3-and-it-looks-like-generated
//       Common/intermediate (= none-platform) code cannot reference generated code in the compilation of platform code.
//       Generated codes are treated as platform code (you'll have to use expect/actual).

@GenerateProxy
interface Adder {
    fun add(a: Int, b: Int): Int
}

private val AdderImpl = object : Adder {
    override fun add(a: Int, b: Int) = a + b
}

expect fun Adder.proxy(intercept: Interceptor): Adder

@GenerateProxy
interface SuspendAdder {
    suspend fun add(a: Int, b: Int): Int
}

private val SuspendAdderImpl = object : SuspendAdder {
    override suspend fun add(a: Int, b: Int) = a + b
}

expect fun SuspendAdder.proxy(suspendIntercept: SuspendInterceptor): SuspendAdder
expect fun ServiceId<SuspendAdder>.proxy(tunnel: Tunnel): SuspendAdder
expect fun ServiceId<SuspendAdder>.service(implementation: SuspendAdder): Service

@GenerateProxy
interface MixedAdder {
    fun add(a: Int, b: Int): Int
    suspend fun subtract(a: Int, b: Int): Int
}

private val MixedAdderImpl = object : MixedAdder {
    override fun add(a: Int, b: Int) = a + b
    override suspend fun subtract(a: Int, b: Int) = a + b
}

expect fun MixedAdder.proxy(intercept: Interceptor, suspendIntercept: SuspendInterceptor): MixedAdder

expect fun createSerializer(): BinarySerializer

@GenerateBinarySerializer(
    baseEncoderClasses = [IntEncoder::class],
    enumClasses = [],
    treeConcreteClasses = [],
    graphConcreteClasses = [],
    withDumper = false,
)
private val ContractSerializer = createSerializer()

expect fun createDumper(dumpValue: Appendable.(value: Any) -> Unit): Dumper

@GenerateDumper(
    treeConcreteClasses = [],
    graphConcreteClasses = [],
)
private val Dumper = createDumper {}

private fun print(function: KFunction<*>, @Suppress("unused") parameters: List<Any?>, invoke: Invocation): Any? {
    println(function.name)
    return invoke()
}

private suspend fun print(function: KFunction<*>, @Suppress("unused") parameters: List<Any?>, invoke: SuspendInvocation): Any? {
    println(function.name)
    return invoke()
}

private val AdderId = ServiceId<SuspendAdder>(1)

private fun Serializer.copy(value: Int): Int {
    val writer = BytesWriter(1000)
    write(writer, value)
    return read(BytesReader(writer.buffer)) as Int
}

class NonePlatformGeneratedTest {
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
