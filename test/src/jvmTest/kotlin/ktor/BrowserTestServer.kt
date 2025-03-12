package ch.softappeal.yass2.ktor

@Suppress("HttpUrlsUsage")
fun main() {
    println("http://$LOCAL_HOST:$PORT/index-js.html")
    println("http://$LOCAL_HOST:$PORT/index-wasm.html")
    Server.start(wait = true)
}
