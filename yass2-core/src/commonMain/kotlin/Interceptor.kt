package ch.softappeal.yass2

// function is String (= function name) instead of KFunction because annotation reflection is not multiplatform.

public typealias Invocation = () -> Any?
public typealias Interceptor = (function: String, parameters: List<Any?>, invoke: Invocation) -> Any?

public typealias SuspendInvocation = suspend () -> Any?
public typealias SuspendInterceptor = suspend (function: String, parameters: List<Any?>, invoke: SuspendInvocation) -> Any?

public operator fun Interceptor.plus(intercept: Interceptor): Interceptor = { function, parameters, invoke ->
    this(function, parameters) { intercept(function, parameters, invoke) }
}

public operator fun SuspendInterceptor.plus(intercept: SuspendInterceptor): SuspendInterceptor = { function, parameters, invoke ->
    this(function, parameters) { intercept(function, parameters, invoke) }
}
