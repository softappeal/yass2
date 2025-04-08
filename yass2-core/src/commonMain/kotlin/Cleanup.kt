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
    var tryException: Exception? = null
    return try {
        tryBlock()
    } catch (e: Exception) {
        tryException = e
        throw tryException
    } finally {
        try {
            finallyBlock()
        } catch (finallyException: Exception) {
            if (tryException == null) throw finallyException
            tryException.addSuppressed(finallyException)
        }
    }
}
