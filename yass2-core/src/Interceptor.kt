package ch.softappeal.yass2.core

import kotlin.reflect.KFunction

public typealias Invocation = suspend () -> Any?
public typealias Interceptor = suspend (function: KFunction<*>, parameters: List<Any?>, invocation: Invocation) -> Any?

public inline operator fun Interceptor.plus(crossinline interceptor: Interceptor): Interceptor =
    { function, parameters, invocation ->
        this(function, parameters) { interceptor(function, parameters, invocation) }
    }

public val PassThroughInterceptor: Interceptor = { _, _, invocation -> invocation() }
