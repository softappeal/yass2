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
    .sortedBy { it.name } // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
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

internal fun Array<Any?>.getContinuation() = last() as Continuation<*>
internal fun Array<Any?>.removeContinuation() = copyOf(lastIndex).asList()

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
        val proxy = newProxyInstance(service.java.classLoader, arrayOf(service.java)) { _, method, arguments ->
            val function = method.kotlinFunction!!
            if (function.isSuspend) {
                invokeSuspendFunction(arguments.getContinuation()) {
                    suspendInterceptor(function, arguments.removeContinuation()) {
                        handleInvocationTargetException { method.invoke(implementation, *arguments) }
                    }
                }
            } else {
                val parameters = arguments ?: emptyArray()
                interceptor(function, parameters.asList()) {
                    handleInvocationTargetException { method.invoke(implementation, *parameters) }
                }
            }
        }
        return (@Suppress("UNCHECKED_CAST") (proxy as S))
    }
}
