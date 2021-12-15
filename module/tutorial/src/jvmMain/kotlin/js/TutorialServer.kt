package ch.softappeal.yass2.tutorial.js

import ch.softappeal.yass2.tutorial.*
import ch.softappeal.yass2.tutorial.contract.generated.*

fun main() {
    println("http://$Host:$Port/index.html")
    createKtorEngine(::invoke)
        .start(wait = true)
}
