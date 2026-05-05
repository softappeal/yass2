package ch.softappeal.yass2.core.remote

public sealed class Message

/**
 * Functions must be suspendable because they need IO.
 * Overloading is not allowed (NOTE: It's not worth it, it's even not possible in JavaScript).
 */
public class Request(public val service: String, public val function: String, public val parameters: List<Any?>) : Message()

public sealed class Reply : Message() {
    public abstract fun process(): Any?
}

public class ValueReply(public val value: Any?) : Reply() {
    override fun process(): Any? = value
}

public class ExceptionReply(public val exception: Exception) : Reply() {
    override fun process(): Nothing = throw exception
}
