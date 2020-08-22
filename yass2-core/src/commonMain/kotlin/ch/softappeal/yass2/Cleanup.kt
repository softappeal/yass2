package ch.softappeal.yass2

public inline fun Exception.addSuppressed(block: () -> Unit): Exception {
    try {
        block()
    } catch (e: Exception) {
        addSuppressed(e)
    }
    return this
}

public inline fun <R> tryFinally(tryBlock: () -> R, finallyBlock: () -> Unit): R {
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
