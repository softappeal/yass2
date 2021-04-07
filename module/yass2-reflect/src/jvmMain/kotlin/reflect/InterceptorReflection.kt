package ch.softappeal.yass2.reflect

import ch.softappeal.yass2.*
import java.lang.reflect.*
import java.lang.reflect.Proxy.*
import kotlin.coroutines.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

public fun KClass<*>.serviceFunctions(): List<KFunction<*>> = memberFunctions
    .filter { it.javaMethod!!.declaringClass != Object::class.java }
    .sortedBy { it.name } // note: support for overloading is not worth it and it's even not possible in JavaScript
    .apply {
        require(map { it.name }.toSet().size == size) { "'${this@serviceFunctions}' has overloaded functions" }
    }

internal inline fun handleInvocationTargetException(action: () -> Any?): Any? = try {
    action()
} catch (e: InvocationTargetException) {
    throw e.cause!!
}

internal fun invokeSuspendFunction(
    continuation: Continuation<*>,
    suspendFunction: suspend () -> Any?,
): Any? = handleInvocationTargetException {
    @Suppress("UNCHECKED_CAST") (suspendFunction as (Continuation<*>) -> Any?)(continuation)
}

public object ReflectionProxyFactory : ProxyFactory {
    override fun <S : Any> create(
        service: KClass<S>,
        implementation: S,
        interceptor: Interceptor,
        suspendInterceptor: SuspendInterceptor,
    ): S {
        service.serviceFunctions().apply {
            checkInterceptors(interceptor, suspendInterceptor, any { !it.isSuspend }, any { it.isSuspend })
        }
        @Suppress("UNCHECKED_CAST")
        return newProxyInstance(service.java.classLoader, arrayOf(service.java)) { _, method, arguments ->
            val function = method.kotlinFunction!!
            if (function.isSuspend) {
                val parameters = arguments.copyOf(arguments.size - 1)
                val continuation = arguments.last() as Continuation<*>
                invokeSuspendFunction(continuation) {
                    suspendInterceptor(function, parameters) {
                        handleInvocationTargetException { method.invoke(implementation, *parameters, continuation) }
                    }
                }
            } else {
                val parameters = arguments ?: arrayOf()
                interceptor(function, parameters) {
                    handleInvocationTargetException { method.invoke(implementation, *parameters) }
                }
            }
        } as S
    }
}
