package ch.softappeal.yass2

import kotlin.reflect.*

public typealias Invocation = suspend () -> Any?
public typealias Interceptor = suspend (function: KFunction<*>, parameters: List<Any?>, invocation: Invocation) -> Any?

public operator fun Interceptor.plus(second: Interceptor): Interceptor = { function, parameters, invocation ->
    this(function, parameters) { second(function, parameters, invocation) }
}

public interface ProxyFactory {
    public fun <S : Any> create(service: KClass<S>, implementation: S, interceptor: Interceptor): S
}

public inline operator fun <reified S : Any> ProxyFactory.invoke(implementation: S, noinline interceptor: Interceptor): S =
    create(S::class, implementation, interceptor)
