@file:Suppress("SpellCheckingInspection")

package ch.softappeal.yass2.ktor

import io.ktor.server.http.content.staticFiles
import java.io.File

fun main() { // TODO: test browser manually
    createWebServer {
        // code
        staticFiles("/wasm", File("./build/wasm/packages/yass2-ktor-test/kotlin"))
        staticFiles("/js", File("./build/js/packages/yass2-ktor-test/kotlin"))
        // sources
        staticFiles("/wasm", File(".")) // wasm
        staticFiles("/", File("."))     // js
    }.start(wait = true)
}
