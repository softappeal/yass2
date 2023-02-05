@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.tutorial.js

import ch.softappeal.yass2.tutorial.*

public fun main() {
    println("http://$LOCAL_HOST:$PORT/index.html")
    createKtorEngine()
        .start(wait = true)
}
