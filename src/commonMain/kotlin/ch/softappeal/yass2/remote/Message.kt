package ch.softappeal.yass2.remote

sealed class Message

class Request(val serviceId: Int, val functionId: Int, val parameters: List<Any?>) : Message()

sealed class Reply : Message() {
    abstract fun process(): Any?
}

class ValueReply(val value: Any?) : Reply() {
    override fun process() = value
}

class ExceptionReply(val exception: Exception) : Reply() {
    override fun process() = throw exception
}
