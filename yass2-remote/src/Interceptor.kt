package ch.softappeal.yass2.remote

import kotlin.reflect.KFunction

public typealias Invocation = suspend () -> Any?

/** NOTE: parameter `function` of [Interceptor] could be used to prevent logging of secrets */
public typealias Interceptor = suspend (function: KFunction<*>, parameters: List<Any?>, invocation: Invocation) -> Any?

public inline operator fun Interceptor.plus(crossinline interceptor: Interceptor): Interceptor =
    { function, parameters, invocation ->
        this(function, parameters) { interceptor(function, parameters, invocation) }
    }

public val PassThroughInterceptor: Interceptor = { _, _, invocation -> invocation() }
