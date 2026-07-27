package ch.softappeal.yass2.generate

import ch.softappeal.yass2.core.forEachSeparator
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.Service
import ch.softappeal.yass2.core.remote.ServiceId
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

private fun CodeWriter.writeFun(head: String, parameter: String, tail: String, body: CodeWriter.() -> Unit) {
    writeLine()
    writeNestedLine("public fun$head(", "): $tail =") {
        writeNestedLine("$parameter,")
    }
    nested { body() }
}

private fun KFunction<*>.hasResult() = returnType.classifier != Unit::class

private fun CodeWriter.writeSignature(function: KFunction<*>) {
    writeNestedLine("override suspend fun ${function.name}(") {
        function.valueParameters.forEachIndexed { parameterIndex, parameter ->
            writeNestedLine("p${parameterIndex + 1}: ${parameter.type.toType()},")
        }
    }
    writeNested(")")
}

private fun KFunction<*>.parameters() = (1..valueParameters.size).joinToString(", ") { "p$it" }

private val KClass<*>.withTypeParameters get() = "<${typeParameters.joinToString { it.name }}>"
private val KClass<*>.serviceName get() = "$qualifiedName${if (typeParameters.isEmpty()) "" else withTypeParameters}"
private val KClass<*>.serviceTypeParameters get() = if (typeParameters.isEmpty()) "" else " $withTypeParameters"

public fun CodeWriter.generateProxy(service: KClass<*>) {
    require(service.java.isInterface) { "'${service.qualifiedName}' must be an interface" }

    val functions = service.memberFunctions
        .filter { it.javaMethod!!.declaringClass != Any::class.java }
        .onEach {
            require(it.isSuspend) { "method '${service.qualifiedName}.${it.name}' must be suspend" }
        }
        .sortedBy { it.name }
        .apply {
            val methodNames = map { it.name }
            require(methodNames.hasNoDuplicates()) { "interface '${service.qualifiedName}' has overloaded methods ${methodNames.duplicates()}" }
        }

    writeFun(
        "${service.serviceTypeParameters} ${service.serviceName}.proxy",
        "interceptor: $CSY.core.Interceptor",
        service.serviceName,
    ) {
        writeNestedLine("object : ${service.serviceName} {", "}") {
            functions.forEachSeparator({ writeLine() }) { function ->
                val hasResult = function.hasResult()
                writeSignature(function)
                if (hasResult) write(": ${function.returnType.toType()}")
                writeLine(" {") {
                    writeNestedLine("${if (hasResult) "return " else ""}interceptor(\"${function.name}\", listOf(${function.parameters()})) {") {
                        writeNestedLine("this@proxy.${function.name}(${function.parameters()})")
                    }
                    writeNested("}")
                }
                if (hasResult) write(" as ${function.returnType.toType()}")
                writeLine()
                writeNestedLine("}")
            }
        }
    }

    writeFun(
        "${service.serviceTypeParameters} ${ServiceId::class.qualifiedName}<${service.serviceName}>.proxy",
        "tunnel: $CSY.core.remote.Tunnel",
        service.serviceName,
    ) {
        writeNestedLine("object : ${service.serviceName} {", "}") {
            functions.forEachSeparator({ writeLine() }) { function ->
                val hasResult = function.hasResult()
                writeSignature(function)
                writeLine(" ${if (hasResult) "=" else "{"}") {
                    writeNestedLine("tunnel(${Request::class.qualifiedName}(id, \"${function.name}\", listOf(${function.parameters()})))") {
                        writeNested(".process()")
                        if (hasResult) write(" as ${function.returnType.toType()}") else writeLine()
                    }
                }
                if (!hasResult) writeNested("}")
                writeLine()
            }
        }
    }

    writeFun(
        "${service.serviceTypeParameters} ${ServiceId::class.qualifiedName}<${service.serviceName}>.service",
        "implementation: ${service.serviceName}",
        Service::class.qualifiedName!!,
    ) {
        writeNestedLine("${Service::class.qualifiedName}(id) { function, parameters ->", "}") {
            writeNestedLine("when (function) {", "}") {
                functions.forEach { function ->
                    writeNestedLine("\"${function.name}\" -> implementation.${function.name}(", ")") {
                        function.valueParameters.forEachIndexed { parameterIndex, parameter ->
                            writeNestedLine("parameters[$parameterIndex] as ${parameter.type.toType()},")
                        }
                    }
                }
                writeNestedLine($$"else -> error(\"service '$id' has no function '$function'\")")
            }
        }
    }
}

public fun CodeWriter.generateProxies(services: List<KClass<*>>) {
    services.forEach { generateProxy(it) }
}
