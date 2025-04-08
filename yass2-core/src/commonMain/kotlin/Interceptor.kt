package ch.softappeal.yass2.core

// function is String (= function name) instead of KFunction because annotation reflection is not multiplatform.

public typealias Invocation = suspend () -> Any?
public typealias Interceptor = suspend (function: String, parameters: List<Any?>, invoke: Invocation) -> Any?

public operator fun Interceptor.plus(intercept: Interceptor): Interceptor = { function, parameters, invoke ->
    this(function, parameters) { intercept(function, parameters, invoke) }
}
