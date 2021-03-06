package ch.softappeal.yass2.remote

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.remote.reflect.*
import kotlinx.coroutines.*
import kotlin.test.*

class RemoteReflectionTest : RemoteGeneratedTest() {
    override val remoteProxyFactoryCreator = ::reflectionRemoteProxyFactoryCreator
    override val invoker = ::reflectionInvoker

    @Test
    fun generatedAndReflection() = runBlocking {
        generatedRemoteProxyFactoryCreator(::reflectionInvoker.tunnel(Services)).test()
    }

    @Test
    fun reflectionAndGenerated() = runBlocking {
        reflectionRemoteProxyFactoryCreator(::generatedInvoker.tunnel(Services)).test()
    }

    @Test
    fun functionMapper() = runBlocking {
        val functionMapper = FunctionMapper(Calculator::class)
        assertEquals("add", functionMapper.toFunction(0).name)
        assertEquals("divide", functionMapper.toFunction(1).name)
        assertEquals(
            "'class ch.softappeal.yass2.contract.Calculator' has no function -1",
            assertFailsWith<IllegalStateException> { functionMapper.toFunction(-1) }.message
        )
        assertEquals(
            "'class ch.softappeal.yass2.contract.Calculator' has no function 2",
            assertFailsWith<IllegalStateException> { functionMapper.toFunction(2) }.message
        )
        assertEquals(0, functionMapper.toId("add"))
        assertEquals(1, functionMapper.toId("divide"))
        assertEquals(
            "'class ch.softappeal.yass2.contract.Calculator' has no function 'echo'",
            assertFailsWith<IllegalStateException> { functionMapper.toId("echo") }.message
        )
    }

    @Test
    fun duplicatedServiceId() {
        assertEquals(
            "duplicated service id's",
            assertFailsWith<IllegalArgumentException> { generateInvoker(listOf(EchoId, EchoId)) }.message
        )
    }
}
