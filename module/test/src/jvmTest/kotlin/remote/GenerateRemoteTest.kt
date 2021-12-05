package ch.softappeal.yass2.remote

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.generate.*
import kotlin.test.*

class GenerateRemoteTest {
    @Test
    fun duplicatedServiceId() {
        assertEquals(
            "duplicated service id",
            assertFailsWith<IllegalArgumentException> { generateInvoker(listOf(EchoId, EchoId)) }.message
        )
    }
}
