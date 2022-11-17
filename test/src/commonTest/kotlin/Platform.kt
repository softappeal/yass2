package ch.softappeal.yass2

enum class Platform {
    Jvm,
    Js,
    Linux,
    Mac,
}

expect fun getPlatform(): Platform

fun Platform.actual() = getPlatform() == this

fun runOnPlatforms(vararg platforms: Platform, block: () -> Unit) {
    if (platforms.contains(getPlatform())) block() else println("only runs on [${platforms.joinToString()}] platforms")
}
