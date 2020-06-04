package ch.softappeal.yass2

inline fun Exception.addSuppressed(block: () -> Unit): Exception {
    try {
        block()
    } catch (e: Exception) {
        // TODO Kotlin 1.4: addSuppressed(e)
    }
    return this
}

inline fun <R> tryFinally(tryBlock: () -> R, finallyBlock: () -> Unit): R {
    var exception: Exception? = null
    try {
        return tryBlock()
    } catch (e: Exception) {
        exception = e
        throw exception
    } finally {
        exception?.addSuppressed(finallyBlock) ?: finallyBlock()
    }
}
