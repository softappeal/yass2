@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.RemotePackage
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.forEach
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.generate.writeFun
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

private fun KFunction<*>.hasResult() = returnType.classifier != Unit::class

private fun CodeWriter.writeSignature(function: KFunction<*>) {
    writeNestedLine("override suspend fun ${function.name}(") {
        function.valueParameters.forEachIndexed { parameterIndex, parameter ->
            writeNestedLine("p${parameterIndex + 1}: ${parameter.type.toType()},")
        }
    }
    writeNested(")")
}

private fun KFunction<*>.parameters() = (1..valueParameters.size).joinToString { "p$it" }

private val KClass<*>.serviceName get() = "$qualifiedName${if (typeParameters.isEmpty()) "" else "<${typeParameters.joinToString { it.name }}>"}"

private val KClass<*>.serviceTypeParameters: String
    get() {
        if (typeParameters.isEmpty()) return ""
        fun KTypeParameter.typeParameter() = buildString {
            append(name)
            val first = upperBounds.firstOrNull() ?: return@buildString
            if (upperBounds.count() != 1) error("not yet implemented: bounds must have size <= 1")
            append(" : ")
            append(first)
        }
        return " <${typeParameters.joinToString { it.typeParameter() }}>"
    }


internal fun CodeWriter.generateProxy(service: KClass<*>) {
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
        "interceptor: $RemotePackage.Interceptor",
        service.serviceName,
    ) {
        writeNestedLine("object : ${service.serviceName} {", "}") {
            functions.forEach({ writeLine() }) { function ->
                val hasResult = function.hasResult()
                writeSignature(function)
                if (hasResult) write(": ${function.returnType.toType()}")
                writeLine(" {") {
                    writeNestedLine("${if (hasResult) "return " else ""}interceptor(${service.serviceName}::${function.name}, listOf(${function.parameters()})) {") {
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
        "tunnel: $RemotePackage.Tunnel",
        service.serviceName,
    ) {
        writeNestedLine("object : ${service.serviceName} {", "}") {
            functions.forEach({ writeLine() }) { function ->
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
