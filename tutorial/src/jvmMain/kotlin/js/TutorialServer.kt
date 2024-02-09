@file:Suppress("HttpUrlsUsage")

package ch.softappeal.yass2.tutorial.js

import ch.softappeal.yass2.tutorial.LOCAL_HOST
import ch.softappeal.yass2.tutorial.PORT
import ch.softappeal.yass2.tutorial.createKtorEngine

public fun main() {
    println("http://$LOCAL_HOST:$PORT")
    createKtorEngine()
        .start(wait = true)
}
