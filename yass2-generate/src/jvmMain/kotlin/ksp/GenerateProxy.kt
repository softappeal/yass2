@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.ksp // TODO: review

import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.Service
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.sortMethods
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

private fun KSFunctionDeclaration.hasResult() = "kotlin.Unit" != returnType!!.resolve().qualifiedName

private fun CodeWriter.writeSignature(function: KSFunctionDeclaration) {
    writeNestedLine("override suspend fun ${function.name}(") {
        function.parameters.forEachIndexed { parameterIndex, parameter ->
            writeNestedLine("p${parameterIndex + 1}: ${parameter.type.toType()},")
        }
    }
    writeNested(")")
}

private fun KSFunctionDeclaration.parameters() = (1..parameters.size).joinToString(", ") { "p$it" }

private val KSClassDeclaration.withTypeParameters get() = "<${typeParameters.joinToString { it.simpleName.asString() }}>"
private val KSClassDeclaration.withTypes get() = "${qualifiedName()}${if (typeParameters.isEmpty()) "" else withTypeParameters}"
private val KSClassDeclaration.types get() = if (typeParameters.isEmpty()) "" else " $withTypeParameters"

internal fun CodeWriter.generateProxy(service: KSClassDeclaration, expectWriter: CodeWriter?) {
    require(service.classKind == ClassKind.INTERFACE) { "${service.qualifiedName()} must be an interface" }

    val functions = service.getAllFunctions().toList()
        .filter { it.name !in AnyFunctions }
        .sortMethods({ service.qualifiedName() }, { name }, { Modifier.SUSPEND in modifiers })

    expectWriter?.let {
        it.writeLine()
        it.writeNestedLine("public expect fun${service.types} ${service.withTypes}.proxy(intercept: $CSY.core.Interceptor): ${service.withTypes}")
    }
    writeLine()
    writeNestedLine("public ${expectWriter.actual()}fun${service.types} ${service.withTypes}.proxy(") {
        writeNestedLine("intercept: $CSY.core.Interceptor,")
    }
    writeNestedLine("): ${service.withTypes} = object : ${service.withTypes} {", "}") {
        functions.forEachIndexed { functionIndex, function ->
            if (functionIndex != 0) writeLine()
            val hasResult = function.hasResult()
            writeSignature(function)
            if (hasResult) write(": ${function.returnType!!.toType()}")
            writeLine(" {") {
                writeNestedLine("${if (hasResult) "return " else ""}intercept(\"${function.name}\", listOf(${function.parameters()})) {") {
                    writeNestedLine("this@proxy.${function.name}(${function.parameters()})")
                }
                writeNested("}")
            }
            if (hasResult) write(" as ${function.returnType!!.toType()}")
            writeLine()
            writeNestedLine("}")
        }
    }

    expectWriter?.writeNestedLine("public expect fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.proxy(tunnel: $CSY.core.remote.Tunnel): ${service.withTypes}")
    writeLine()
    writeNestedLine("public ${expectWriter.actual()}fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.proxy(") {
        writeNestedLine("tunnel: $CSY.core.remote.Tunnel,")
    }
    writeNestedLine("): ${service.withTypes} =") {
        writeNestedLine("object : ${service.withTypes} {", "}") {
            functions.forEachIndexed { functionIndex, function ->
                if (functionIndex != 0) writeLine()
                val hasResult = function.hasResult()
                writeSignature(function)
                writeLine(" ${if (hasResult) "=" else "{"}") {
                    writeNestedLine("tunnel(${Request::class.qualifiedName}(id, \"${function.name}\", listOf(${function.parameters()})))") {
                        writeNested(".process()")
                        if (hasResult) write(" as ${function.returnType!!.toType()}") else writeLine()
                    }
                }
                if (!hasResult) writeNested("}")
                writeLine()
            }
        }
    }

    expectWriter?.writeNestedLine("public expect fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.service(implementation: ${service.withTypes}): ${Service::class.qualifiedName}")
    writeLine()
    writeNestedLine("public ${expectWriter.actual()}fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.service(") {
        writeNestedLine("implementation: ${service.withTypes},")
    }
    writeNestedLine("): ${Service::class.qualifiedName} =") {
        writeNestedLine("${Service::class.qualifiedName}(id) { function, parameters ->", "}") {
            writeNestedLine("when (function) {", "}") {
                functions.forEach { function ->
                    writeNestedLine("\"${function.name}\" -> implementation.${function.name}(", ")") {
                        function.parameters.forEachIndexed { parameterIndex, parameter ->
                            writeNestedLine("parameters[$parameterIndex] as ${parameter.type.toType()},")
                        }
                    }
                }
                writeNestedLine("else -> error(\"service '${'$'}id' has no function '${'$'}function'\")")
            }
        }
    }
}

private val AnyFunctions = setOf("toString", "equals", "hashCode")
