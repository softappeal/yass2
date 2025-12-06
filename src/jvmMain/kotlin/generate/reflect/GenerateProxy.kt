@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.forEachSeparator
import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
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

private fun KFunction<*>.parameters() = (1..valueParameters.size).joinToString(", ") { "p$it" }

private val KClass<*>.withTypeParameters get() = "<${typeParameters.joinToString { it.name }}>"
private val KClass<*>.withTypes get() = "$qualifiedName${if (typeParameters.isEmpty()) "" else withTypeParameters}"
private val KClass<*>.types get() = if (typeParameters.isEmpty()) "" else " $withTypeParameters"

internal fun CodeWriter.generateProxy(service: KClass<*>) {
    require(service.java.isInterface) { "${service.qualifiedName} must be an interface" }

    val functions = service.memberFunctions
        .filter { it.javaMethod!!.declaringClass != Any::class.java }
        .onEach {
            require(it.isSuspend) { "method ${service.qualifiedName}.${it.name} must be suspend" }
        }
        .sortedBy { it.name }
        .apply {
            val methodNames = map { it.name }
            require(methodNames.hasNoDuplicates()) { "interface ${service.qualifiedName} has overloaded methods ${methodNames.duplicates()}" }
        }

    writeLine()
    writeNestedLine("public fun${service.types} ${service.withTypes}.proxy(") {
        writeNestedLine("intercept: $CSY.Interceptor,")
    }
    writeNestedLine("): ${service.withTypes} = object : ${service.withTypes} {", "}") {
        functions.forEachSeparator({ writeLine() }) { function ->
            val hasResult = function.hasResult()
            writeSignature(function)
            if (hasResult) write(": ${function.returnType.toType()}")
            writeLine(" {") {
                writeNestedLine("${if (hasResult) "return " else ""}intercept(\"${function.name}\", listOf(${function.parameters()})) {") {
                    writeNestedLine("this@proxy.${function.name}(${function.parameters()})")
                }
                writeNested("}")
            }
            if (hasResult) write(" as ${function.returnType.toType()}")
            writeLine()
            writeNestedLine("}")
        }
    }

    writeLine()
    writeNestedLine("public fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.proxy(") {
        writeNestedLine("tunnel: $CSY.remote.Tunnel,")
    }
    writeNestedLine("): ${service.withTypes} =") {
        writeNestedLine("object : ${service.withTypes} {", "}") {
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

    writeLine()
    writeNestedLine("public fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.service(") {
        writeNestedLine("implementation: ${service.withTypes},")
    }
    writeNestedLine("): ${Service::class.qualifiedName} =") {
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
