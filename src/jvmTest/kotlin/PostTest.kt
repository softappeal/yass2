package demo

import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test

val Server = embeddedServer(io.ktor.server.cio.CIO, PORT) {
    routing {
        route(PATH) {
            post {
                call.respond("hello")
            }
        }
        staticFiles("/js", File("./build/js/packages/demo-test/kotlin"))
        staticFiles("/wasm", File("./build/js/packages/demo-wasm-js-test/kotlin"))
    }
}

class PostTest {
    @Test
    fun test() {
        Server.start()
        try {
            runBlocking {
                postRequest(io.ktor.client.engine.cio.CIO)
            }
        } finally {
            Server.stop()
        }
    }
}
