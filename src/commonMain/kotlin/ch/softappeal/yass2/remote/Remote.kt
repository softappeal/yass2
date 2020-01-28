package ch.softappeal.yass2.remote

import kotlin.reflect.*

class ServiceId<S : Any> @PublishedApi internal constructor(val service: KClass<S>, val id: Int)

inline fun <reified S : Any> serviceId(id: Int): ServiceId<S> = ServiceId(S::class, id)

interface RemoteProxyFactory {
    fun <S : Any> create(serviceId: ServiceId<S>): S
}

operator fun <S : Any> RemoteProxyFactory.invoke(serviceId: ServiceId<S>): S = create(serviceId)

class Service internal constructor(val serviceId: ServiceId<*>, val implementation: Any)

operator fun <S : Any> ServiceId<S>.invoke(service: S): Service = Service(this, service)

typealias Tunnel = suspend (request: Request) -> Reply

typealias Invoker = suspend (request: Request, service: Service) -> Any?

fun Invoker.tunnel(services: Collection<Service>): Tunnel {
    val id2service = services.associateBy { it.serviceId.id }
    require(id2service.size == services.size) { "duplicated service id's" }
    return { request ->
        try {
            val result = this(request, id2service[request.serviceId] ?: error("no service id ${request.serviceId}"))
            ValueReply(if (result === Unit) null else result)
        } catch (e: Exception) {
            ExceptionReply(e)
        }
    }
}

typealias RemoteProxyFactoryCreator = (tunnel: Tunnel) -> RemoteProxyFactory

fun missingFunction(request: Request): Unit = error("no function id ${request.functionId} for service id ${request.serviceId}")
