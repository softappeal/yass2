package ch.softappeal.yass2

import kotlin.reflect.*

public typealias Invocation = () -> Any?
public typealias Interceptor = (function: KFunction<*>, parameters: List<Any?>, invoke: Invocation) -> Any?

public typealias SuspendInvocation = suspend () -> Any?
public typealias SuspendInterceptor = suspend (function: KFunction<*>, parameters: List<Any?>, invoke: SuspendInvocation) -> Any?

public operator fun Interceptor.plus(intercept: Interceptor): Interceptor = { function, parameters, invoke ->
    this(function, parameters) { intercept(function, parameters, invoke) }
}

public operator fun SuspendInterceptor.plus(intercept: SuspendInterceptor): SuspendInterceptor = { function, parameters, invoke ->
    this(function, parameters) { intercept(function, parameters, invoke) }
}

@Target(AnnotationTarget.CLASS)
public annotation class Proxy
