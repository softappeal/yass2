package ch.softappeal.yass2

import kotlin.reflect.*

public typealias Invocation = () -> Any?
public typealias Interceptor = (function: KFunction<*>, parameters: Array<Any?>, invocation: Invocation) -> Any?

public typealias SuspendInvocation = suspend () -> Any?
public typealias SuspendInterceptor = suspend (function: KFunction<*>, parameters: Array<Any?>, invocation: SuspendInvocation) -> Any?

public operator fun Interceptor.plus(second: Interceptor): Interceptor = { function, parameters, invocation ->
    this(function, parameters) { second(function, parameters, invocation) }
}

public operator fun SuspendInterceptor.plus(second: SuspendInterceptor): SuspendInterceptor = { function, parameters, invocation ->
    this(function, parameters) { second(function, parameters, invocation) }
}

public interface ProxyFactory {
    public fun <S : Any> create(
        service: KClass<S>, implementation: S, interceptor: Interceptor, suspendInterceptor: SuspendInterceptor
    ): S
}

public inline operator fun <reified S : Any> ProxyFactory.invoke(
    implementation: S, noinline interceptor: Interceptor, noinline suspendInterceptor: SuspendInterceptor
): S = create(S::class, implementation, interceptor, suspendInterceptor)

public val MissingInterceptor: Interceptor = { _, _, _ -> throw RuntimeException("missing Interceptor") }

public val MissingSuspendInterceptor: SuspendInterceptor = { _, _, _ -> throw RuntimeException("missing SuspendInterceptor") }

public inline operator fun <reified S : Any> ProxyFactory.invoke(implementation: S, noinline interceptor: Interceptor): S =
    this(implementation, interceptor, MissingSuspendInterceptor)

public inline operator fun <reified S : Any> ProxyFactory.invoke(implementation: S, noinline interceptor: SuspendInterceptor): S =
    this(implementation, MissingInterceptor, interceptor)

public fun checkInterceptors(
    interceptor: Interceptor, suspendInterceptor: SuspendInterceptor,
    needsInterceptor: Boolean, needsSuspendInterceptor: Boolean
) {
    if (needsInterceptor) require(interceptor !== MissingInterceptor) { "missing Interceptor" }
    if (needsSuspendInterceptor) require(suspendInterceptor !== MissingSuspendInterceptor) { "missing SuspendInterceptor" }
}
