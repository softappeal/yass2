package ch.softappeal.yass2.core.remote

public class ServiceId<@Suppress("unused") S : Any>(public val id: String)

public class Service(
    internal val id: String,
    internal val invoke: suspend (function: String, parameters: List<Any?>) -> Any?,
)

public typealias Tunnel = suspend (request: Request) -> Reply

public fun tunnel(vararg services: Service): Tunnel {
    val serviceId2service = services.associateBy(Service::id)
    require(serviceId2service.size == services.size) { "duplicated service" }
    return { request ->
        val service = serviceId2service[request.service] ?: error("no service '${request.service}'")
        try {
            val result = service.invoke(request.function, request.parameters)
            ValueReply(if (result === Unit) null else result)
        } catch (e: ContractException) { // we only return contract exceptions; others are propagated
            ExceptionReply(e)
        }
    }
}
