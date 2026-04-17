package ch.softappeal.yass2.core

import kotlin.reflect.KClass

// function is String (= function name) instead of KFunction because annotation reflection is not multiplatform.

public typealias Invocation = suspend () -> Any?
public typealias Interceptor = suspend (function: String, parameters: List<Any?>, invocation: Invocation) -> Any?

public inline operator fun Interceptor.plus(crossinline interceptor: Interceptor): Interceptor =
    { function, parameters, invocation ->
        this(function, parameters) { interceptor(function, parameters, invocation) }
    }

public val PassThroughInterceptor: Interceptor = { _, _, invocation -> invocation() }

public annotation class Proxies(vararg val value: KClass<*>)
