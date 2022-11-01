package transport.ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.test.*

// see https://youtrack.jetbrains.com/issue/KTOR-5093/Native-Read-from-a-closed-socket-doesnt-throw-an-exception

@Ignore // TODO: remove @Ignore for running tests
class KtorReadingFromClosedSocketBugTest {
    @Test
    fun `Native - Read from a closed socket doesn't throw an exception`() {
        val address = InetSocketAddress("localhost", 12345)
        val tcp = aSocket(SelectorManager(EmptyCoroutineContext)).tcp()
        val serverSocket = tcp.bind(address)
        runBlocking {
            launch {
                serverSocket.accept()
                println("client connected")
            }
            val clientSocket = tcp.connect(address)
            val clientReadChannel = clientSocket.openReadChannel()
            launch {
                try {
                    println("reading from clientSocket ...")
                    clientReadChannel.readByte()
                } catch (e: Exception) {
                    println("exception is correctly thrown for jvm but not for native targets")
                }
            }
            delay(1_000)
            clientSocket.close()
            println("clientSocket closed")
        }
        /*

        OUTPUT
        ======

        jvm target
        ----------
        client connected
        clientSocket closed
        reading from clientSocket ...
        exception is correctly thrown for jvm but not for native targets

        native targets
        --------------
        client connected
        clientSocket closed
        reading from clientSocket ...
        <no exception here, reading from clientSocket never returns>

        */
    }
}
