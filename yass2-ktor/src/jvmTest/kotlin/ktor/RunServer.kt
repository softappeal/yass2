package ch.softappeal.yass2.ktor

@Suppress("HttpUrlsUsage")
fun main() { // TODO: test browser manually
    println("http://$LOCAL_HOST:$PORT/js/")
    println("http://$LOCAL_HOST:$PORT/wasm/")
    Server.start(wait = true)
}
