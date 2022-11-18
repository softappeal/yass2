package ch.softappeal.yass2.remote

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import kotlin.test.*

class GenerateRemoteTest {
    @Test
    fun duplicatedServiceId() = assertFailsMessage<IllegalArgumentException>("duplicated service id") {
        generateInvoke(listOf(EchoId, EchoId))
    }
}
