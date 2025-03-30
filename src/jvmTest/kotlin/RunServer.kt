package demo

@Suppress("HttpUrlsUsage")
fun main() {
    println("http://$LOCAL_HOST:$PORT/js/")
    println("http://$LOCAL_HOST:$PORT/wasm/")
    Server.start(wait = true)
}
