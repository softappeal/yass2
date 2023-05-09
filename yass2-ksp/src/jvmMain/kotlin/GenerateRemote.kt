package ch.softappeal.yass2.ksp

import ch.softappeal.yass2.remote.*
import kotlin.reflect.*
import kotlin.reflect.full.*

private fun KClass<*>.suspendServiceFunctions(): List<KFunction<*>> = serviceFunctions().onEach {
    require(it.isSuspend) { "'$it' is not a suspend function" }
}

public fun Appendable.generateRemote(
    serviceIds: List<ServiceId<*>>,
) {
    require(serviceIds.map { it.id }.toSet().size == serviceIds.size) { "duplicated service id" }
    generateRemoteProxyFactory(serviceIds)
    generateInvoke(serviceIds)
}

private fun Appendable.generateRemoteProxyFactory(
    serviceIds: List<ServiceId<*>>,
) {
    write("""

        @Suppress("RedundantSuppression", "UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
        public fun generatedRemoteProxyFactory(
            tunnel: $CSY.remote.Tunnel,
        ): ${RemoteProxyFactory::class.qualifiedName} =
            object : ${RemoteProxyFactory::class.qualifiedName} {
                suspend operator fun $CSY.remote.Tunnel.invoke(serviceId: Int, functionId: Int, vararg parameters: Any?): Any? =
                    this(${Request::class.qualifiedName}(serviceId, functionId, listOf(*parameters))).process()
        
                override fun <S : Any> create(serviceId: ${ServiceId::class.qualifiedName}<S>): S = when (serviceId.id) {
    """)
    serviceIds.forEach { serviceId ->
        write("""
            ${serviceId.id} -> object : ${serviceId.service.qualifiedName} {
        """, 3)
        serviceId.service.suspendServiceFunctions().forEachIndexed { functionIndex, function ->
            with(function) {
                if (functionIndex != 0) appendLine()
                writeFunctionSignature("                ", this)
                if (hasResult()) append(" = ") else append(" {\n                    ")
                append("tunnel(${serviceId.id}, $functionIndex")
                valueParameters.forEach { append(", p${it.index}") }
                append(")")
                if (hasResult() && returnType.needsCast()) append(" as $returnType")
                if (!hasResult()) append("\n                }")
                appendLine()
            }
        }
        write("""
            } as S
        """, 3)
    }
    write("""
                else -> error("no service id ${'$'}{serviceId.id}")
            }
        }
    """, 1)
}

private fun Appendable.generateInvoke(
    serviceIds: List<ServiceId<*>>,
) {
    write("""

        @Suppress("RedundantSuppression", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "RedundantNullableReturnType")
        public suspend fun generatedInvoke(
            request: ${Request::class.qualifiedName}, service: ${Service::class.qualifiedName},
        ): Any? {
            val p = request.parameters
            return when (request.serviceId) {
    """)
    serviceIds.forEach { serviceId ->
        write("""
            ${serviceId.id} -> {
                val i = service.implementation as ${serviceId.service.qualifiedName}
                when (request.functionId) {
        """, 2)
        serviceId.service.suspendServiceFunctions().forEachIndexed { functionIndex, function ->
            append("                $functionIndex -> i.${function.name}(")
            function.valueParameters.forEach { parameter ->
                if (parameter.index != 1) append(", ")
                append("p[${parameter.index - 1}]")
                if (parameter.type.needsCast()) append(" as ${parameter.type}")
            }
            appendLine(")")
        }
        write("""
                    else -> $CSY.remote.missingFunction(request)
                }
            }
        """, 2)
    }
    write("""
                else -> error("no service id ${'$'}{request.serviceId}")
            }
        }
    """)
}
