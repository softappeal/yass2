package ch.softappeal.yass2.remote

public class ServiceId<@Suppress("unused") S : Any>(public val id: String)

public class Service(
    internal val serviceId: String,
    internal val tunnel: suspend (function: String, parameters: List<Any?>) -> Any?,
)

public typealias Tunnel = suspend (request: Request) -> Reply

public fun tunnel(vararg services: Service): Tunnel {
    val serviceId2service = services.associateBy(Service::serviceId)
    require(serviceId2service.size == services.size) { "duplicated service" }
    return { request ->
        val service = serviceId2service[request.service] ?: error("no service '${request.service}'")
        try {
            val result = service.tunnel(request.function, request.parameters)
            ValueReply(if (result === Unit) null else result)
        } catch (e: Exception) {
            ExceptionReply(e)
        }
    }
}
