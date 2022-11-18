@file:Suppress("GrazieInspection")

package ch.softappeal.yass2.transport.ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.random.*
import kotlin.test.*

// see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception

@Ignore // comment out this line for running tests
class KtorReadingFromClosedSocketBugTest {
    @Test
    fun `Native - Read from a closed socket doesn't throw an exception`() {
        val address = InetSocketAddress("localhost", Random.nextInt(2_000..65_000))
        val tcp = aSocket(SelectorManager(EmptyCoroutineContext)).tcp()
        val serverSocket = tcp.bind(address)
        runBlocking {
            launch {
                serverSocket.accept()
                println("client connected")
            }
            val clientSocket = tcp.connect(address)
            launch {
                val clientReadChannel = clientSocket.openReadChannel()
                println("clientSocket.isClosed: ${clientSocket.isClosed}")
                println("clientReadChannel.isClosedForRead: ${clientReadChannel.isClosedForRead}")
                delay(2_000)
                println("clientSocket.isClosed: ${clientSocket.isClosed}")
                println("clientReadChannel.isClosedForRead: ${clientReadChannel.isClosedForRead}")
                try {
                    println("trying to read from clientReadChannel")
                    clientReadChannel.readByte()
                    println("reading from clientReadChannel succeeded^")
                } catch (e: Exception) {
                    println("reading from clientReadChannel failed")
                }
            }
            delay(1_000)
            clientSocket.close()
            println("clientSocket closed")
        }
        /*
            jvm target (expected behavior)              native target (erroneous behavior)
            ------------------------------              ----------------------------------
            client connected                            client connected
            clientSocket.isClosed: false                clientSocket.isClosed: false
            clientReadChannel.isClosedForRead: false    clientReadChannel.isClosedForRead: false
            clientSocket closed                         clientSocket closed
            clientSocket.isClosed: true                 clientSocket.isClosed: false
            clientReadChannel.isClosedForRead: true     clientReadChannel.isClosedForRead: false
            trying to read from clientReadChannel       trying to read from clientReadChannel
            reading from clientReadChannel failed       <reading from clientReadChannel never returns>
        */
    }
}
