package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.remote.reflect.*
import kotlinx.coroutines.*
import kotlin.test.*

class ReflectionRemoteTest : RemoteTest() {
    override val invoke = ::reflectionInvoke
    override val remoteProxyFactory = ::reflectionRemoteProxyFactory

    @Test
    fun functionMapper() = runBlocking {
        val functionMapper = FunctionMapper(Calculator::class)
        assertEquals("add", functionMapper.toFunction(0).name)
        assertEquals("divide", functionMapper.toFunction(1).name)
        assertFailsMessage<IllegalStateException>("'class ch.softappeal.yass2.contract.Calculator' has no function -1") { functionMapper.toFunction(-1) }
        assertFailsMessage<IllegalStateException>("'class ch.softappeal.yass2.contract.Calculator' has no function 2") { functionMapper.toFunction(2) }
        assertEquals(0, functionMapper.toId("add"))
        assertEquals(1, functionMapper.toId("divide"))
        assertFailsMessage<IllegalStateException>("'class ch.softappeal.yass2.contract.Calculator' has no function 'echo'") { functionMapper.toId("echo") }
    }

    @Test
    fun duplicatedServiceId() = assertFailsMessage<IllegalArgumentException>("duplicated service id") {
        generateInvoke(listOf(EchoId, EchoId))
    }
}
