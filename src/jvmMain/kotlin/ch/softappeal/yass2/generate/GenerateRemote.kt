package ch.softappeal.yass2.generate

import ch.softappeal.yass2.remote.*
import kotlin.reflect.full.*

fun generateRemote(
    serviceIds: List<ServiceId<*>>,
    remoteProxyFactoryCreator: String = "generatedRemoteProxyFactoryCreator", invoker: String = "generatedInvoker"
): String = writer {
    require(serviceIds.map { it.id }.toSet().size == serviceIds.size) { "duplicated service id's" }
    write("""
        @Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection")
        fun $remoteProxyFactoryCreator(tunnel: $CSY.remote.Tunnel) = object : ${RemoteProxyFactory::class.qualifiedName} {
            suspend operator fun $CSY.remote.Tunnel.invoke(serviceId: Int, functionId: Int, vararg parameters: Any?): Any? =
                this(${Request::class.qualifiedName}(serviceId, functionId, listOf(*parameters))).process()
        
            override fun <S : Any> create(serviceId: ${ServiceId::class.qualifiedName}<S>): S = when (serviceId.id) {
    """)
    serviceIds.forEach { serviceId ->
        write("""
            ${serviceId.id} -> object : ${serviceId.service.qualifiedName} {
        """, 2)
        serviceId.service.suspendServiceFunctions().withIndex().forEach { (functionIndex, function) ->
            with(function) {
                if (functionIndex != 0) append('\n')
                writeFunctionSignature(this)
                if (hasResult()) append(" = ") else append(" {\n                ")
                append("tunnel(${serviceId.id}, $functionIndex")
                valueParameters.forEach { append(", p${it.index}") }
                append(")")
                if (hasResult() && returnType.needsCast()) append(" as $returnType")
                if (!hasResult()) append("\n            }")
                append("\n")
            }
        }
        write("""
            } as S
        """, 2)
    }
    write("""
                else -> error("no service id ${'$'}{serviceId.id}")
            }
        }

        @Suppress("RemoveRedundantQualifierName", "SpellCheckingInspection")
        suspend fun $invoker(request: ${Request::class.qualifiedName}, service: ${Service::class.qualifiedName}): Any? {
            val p = request.parameters
            return when (request.serviceId) {
    """)
    serviceIds.forEach { serviceId ->
        write("""
            ${serviceId.id} -> {
                val i = service.implementation as ${serviceId.service.qualifiedName}
                when (request.functionId) {
        """, 2)
        serviceId.service.suspendServiceFunctions().withIndex().forEach { (functionIndex, function) ->
            append("                $functionIndex -> i.${function.name}(")
            function.valueParameters.forEach { parameter ->
                if (parameter.index != 1) append(", ")
                append("p[${parameter.index - 1}]")
                if (parameter.type.needsCast()) append(" as ${parameter.type}")
            }
            append(")\n")
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
