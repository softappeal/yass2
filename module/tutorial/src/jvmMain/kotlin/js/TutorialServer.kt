@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.tutorial.js

import ch.softappeal.yass2.tutorial.*

public fun main() {
    println("http://$Host:$Port/index.html")
    createKtorEngine()
        .start(wait = true)
}
