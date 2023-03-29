package ch.softappeal.yass2.remote.reflect

import ch.softappeal.yass2.reflect.*
import ch.softappeal.yass2.remote.*
import java.lang.reflect.Proxy.*
import java.util.concurrent.*
import kotlin.reflect.*
import kotlin.reflect.full.*

public fun KClass<*>.suspendServiceFunctions(): List<KFunction<*>> = serviceFunctions().onEach {
    require(it.isSuspend) { "'$it' is not a suspend function" }
}

public class FunctionMapper(private val service: KClass<*>) {
    private val functions: Array<KFunction<*>> = service.suspendServiceFunctions().toTypedArray()
    private val name2id: Map<String, Int> = functions.withIndex().associate { (index, function) -> function.name to index }
    public fun toFunction(id: Int): KFunction<*> = if (id >= 0 && id < functions.size) functions[id] else error("'$service' has no function $id")
    public fun toId(name: String): Int = name2id[name] ?: error("'$service' has no function '$name'")
}

private val functionMappers = ConcurrentHashMap<KClass<*>, FunctionMapper>(16)
private fun functionMapper(service: KClass<*>): FunctionMapper = functionMappers.computeIfAbsent(service) { FunctionMapper(service) }

public fun reflectionRemoteProxyFactory(tunnel: Tunnel): RemoteProxyFactory = object : RemoteProxyFactory {
    override fun <S : Any> create(serviceId: ServiceId<S>): S {
        val functionMapper = functionMapper(serviceId.service)
        val javaService = serviceId.service.java
        val proxy = newProxyInstance(javaService.classLoader, arrayOf(javaService)) { _, method, arguments ->
            invokeSuspendFunction(arguments.getContinuation()) {
                tunnel(
                    Request(serviceId.id, functionMapper.toId(method.name), arguments.removeContinuation())
                ).process()
            }
        }
        return (@Suppress("UNCHECKED_CAST") (proxy as S))
    }
}

public suspend fun reflectionInvoke(request: Request, service: Service): Any? = handleInvocationTargetException {
    functionMapper(service.serviceId.service)
        .toFunction(request.functionId)
        .callSuspend(service.implementation, *request.parameters.toTypedArray())
}
