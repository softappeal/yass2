package ch.softappeal.yass2

import kotlin.reflect.*

public typealias SuspendInvocation = suspend () -> Any?
public typealias SuspendInterceptor = suspend (function: KFunction<*>, parameters: List<Any?>, invocation: SuspendInvocation) -> Any?

public operator fun SuspendInterceptor.plus(second: SuspendInterceptor): SuspendInterceptor = { function, parameters, invocation ->
    this(function, parameters) { second(function, parameters, invocation) }
}

public interface ProxyFactory {
    public fun <S : Any> create(service: KClass<S>, implementation: S, interceptor: SuspendInterceptor): S
}

public inline operator fun <reified S : Any> ProxyFactory.invoke(implementation: S, noinline interceptor: SuspendInterceptor): S =
    create(S::class, implementation, interceptor)
