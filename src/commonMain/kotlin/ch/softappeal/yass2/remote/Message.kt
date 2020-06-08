package ch.softappeal.yass2.remote

public sealed class Message

public class Request(public val serviceId: Int, public val functionId: Int, public val parameters: List<Any?>) : Message()

public sealed class Reply : Message() {
    public abstract fun process(): Any?
}

public class ValueReply(public val value: Any?) : Reply() {
    override fun process(): Any? = value
}

public class ExceptionReply(public val exception: Exception) : Reply() {
    override fun process(): Nothing = throw exception
}
