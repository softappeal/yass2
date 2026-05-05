package ch.softappeal.yass2.core

public inline fun <E : Exception> E.addSuppressed(block: () -> Unit): E {
    try {
        block()
    } catch (e: Exception) {
        addSuppressed(e)
    }
    return this
}

public inline fun <R> tryFinally(tryBlock: () -> R, finallyBlock: () -> Unit): R {
    val result = try {
        tryBlock()
    } catch (tryException: Exception) {
        try {
            finallyBlock()
        } catch (finallyException: Exception) {
            tryException.addSuppressed(finallyException)
        }
        throw tryException
    }
    finallyBlock()
    return result
}
