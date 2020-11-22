package ch.softappeal.yass2

/** Calls the specified function [block], adds the thrown exception to `this` and returns `this`. */
public inline fun <E : Exception> E.addSuppressed(block: () -> Unit): E {
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
