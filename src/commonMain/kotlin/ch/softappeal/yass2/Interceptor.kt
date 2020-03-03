package ch.softappeal.yass2

import kotlin.reflect.*

typealias Invocation = () -> Any?
typealias Interceptor = (function: KFunction<*>, parameters: Array<Any?>, invocation: Invocation) -> Any?

typealias SuspendInvocation = suspend () -> Any?
typealias SuspendInterceptor = suspend (function: KFunction<*>, parameters: Array<Any?>, invocation: SuspendInvocation) -> Any?

operator fun Interceptor.plus(second: Interceptor): Interceptor = { function, parameters, invocation ->
    this(function, parameters) { second(function, parameters, invocation) }
}

operator fun SuspendInterceptor.plus(second: SuspendInterceptor): SuspendInterceptor = { function, parameters, invocation ->
    this(function, parameters) { second(function, parameters, invocation) }
}

interface ProxyFactory {
    fun <S : Any> create(service: KClass<S>, implementation: S, interceptor: Interceptor, suspendInterceptor: SuspendInterceptor): S
}

inline operator fun <reified S : Any> ProxyFactory.invoke(
    implementation: S, noinline interceptor: Interceptor, noinline suspendInterceptor: SuspendInterceptor
): S = create(S::class, implementation, interceptor, suspendInterceptor)

@PublishedApi
internal val MissingInterceptor: Interceptor = { _, _, _ -> throw RuntimeException("missing Interceptor") }

@PublishedApi
internal val MissingSuspendInterceptor: SuspendInterceptor = { _, _, _ -> throw RuntimeException("missing SuspendInterceptor") }

inline operator fun <reified S : Any> ProxyFactory.invoke(implementation: S, noinline interceptor: Interceptor): S =
    this(implementation, interceptor, MissingSuspendInterceptor)

inline operator fun <reified S : Any> ProxyFactory.invoke(implementation: S, noinline interceptor: SuspendInterceptor): S =
    this(implementation, MissingInterceptor, interceptor)

fun checkInterceptors(
    interceptor: Interceptor, suspendInterceptor: SuspendInterceptor,
    needsInterceptor: Boolean, needsSuspendInterceptor: Boolean
) {
    if (needsInterceptor) require(interceptor !== MissingInterceptor) { "missing Interceptor" }
    if (needsSuspendInterceptor) require(suspendInterceptor !== MissingSuspendInterceptor) { "missing SuspendInterceptor" }
}
