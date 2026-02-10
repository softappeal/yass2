package ch.softappeal.yass2.ktor

@Suppress("HttpUrlsUsage")
fun main() { // TODO: test browser manually
    println("http://$LOCAL_HOST:$PORT/wasm/")
    println("http://$LOCAL_HOST:$PORT/js/")
    Server.start(wait = true)
}
