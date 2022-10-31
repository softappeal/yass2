package transport.ktor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.test.*

@Ignore // TODO: remove @Ignore for running tests
class KtorReadingFromClosedSocketBugTest {
    @Test
    fun `reading from a closed socket must throw an exception on any target`() {
        val address = InetSocketAddress("localhost", 12345)
        val tcp = aSocket(SelectorManager(EmptyCoroutineContext)).tcp()
        val serverSocket = tcp.bind(address)
        runBlocking {
            launch { serverSocket.accept() }
            val clientSocket = tcp.connect(address)
            val clientReadChannel = clientSocket.openReadChannel()
            launch {
                try {
                    println("reading from clientSocket ...")
                    clientReadChannel.readByte()
                } catch (e: Exception) {
                    println("exception is thrown on jvm, but not on linuxX64 or macosArm64 targets: $e")
                }
            }
            clientSocket.close()
            println("clientSocket closed")
        }
        /*

        OUTPUT
        ======

        jvm target
        ----------
        clientSocket closed
        reading from clientSocket ...
        exception is thrown on jvm, but not on linuxX64 or macosArm64 targets: kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job="cio-from-nio-reader#4":StandaloneCoroutine{Cancelled}@487db668

        other targets
        -------------
        clientSocket closed
        reading from clientSocket ...
        <no exception here, reading from clientSocket never returns>

        */
    }
}
