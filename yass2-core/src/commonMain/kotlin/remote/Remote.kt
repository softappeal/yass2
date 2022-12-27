package ch.softappeal.yass2.remote

import kotlin.reflect.*

public class ServiceId<S : Any> @PublishedApi internal constructor(public val service: KClass<S>, public val id: Int)

public inline fun <reified S : Any> serviceId(id: Int): ServiceId<S> = ServiceId(S::class, id)

public interface RemoteProxyFactory {
    public fun <S : Any> create(serviceId: ServiceId<S>): S
}

public operator fun <S : Any> RemoteProxyFactory.invoke(serviceId: ServiceId<S>): S = create(serviceId)

public class Service internal constructor(public val serviceId: ServiceId<*>, public val implementation: Any)

public operator fun <S : Any> ServiceId<S>.invoke(service: S): Service = Service(this, service)

public typealias Tunnel = suspend (request: Request) -> Reply

public typealias Invoker = suspend (request: Request, service: Service) -> Any?

public fun Invoker.tunnel(vararg services: Service): Tunnel {
    val id2service = services.associateBy { it.serviceId.id }
    require(id2service.size == services.size) { "duplicated service id" }
    return { request ->
        try {
            val result = this(request, id2service[request.serviceId] ?: error("no service id ${request.serviceId}"))
            ValueReply(if (result === Unit) null else result)
        } catch (e: Exception) {
            ExceptionReply(e)
        }
    }
}

public fun missingFunction(request: Request): Nothing {
    error("no function id ${request.functionId} for service id ${request.serviceId}")
}
