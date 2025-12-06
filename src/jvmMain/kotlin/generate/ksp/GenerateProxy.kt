@file:Suppress("DuplicatedCode")

package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.forEachSeparator
import ch.softappeal.yass2.generate.CSY
import ch.softappeal.yass2.generate.CodeWriter
import ch.softappeal.yass2.generate.duplicates
import ch.softappeal.yass2.generate.hasNoDuplicates
import ch.softappeal.yass2.remote.Request
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
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

internal fun CodeWriter.generateProxy(service: KSClassDeclaration) {
    require(service.classKind == ClassKind.INTERFACE) { "${service.qualifiedName()} must be an interface" }

    val functions = service.getAllFunctions().toList()
        .filter { it.name !in AnyFunctions }
        .onEach {
            require(Modifier.SUSPEND in it.modifiers) { "method ${service.qualifiedName()}.${it.name} must be suspend" }
        }
        .sortedBy { it.name }
        .apply {
            val methodNames = map { it.name }
            require(methodNames.hasNoDuplicates()) { "interface ${service.qualifiedName()} has overloaded methods ${methodNames.duplicates()}" }
        }

    writeLine()
    writeNestedLine("public fun${service.types} ${service.withTypes}.proxy(") {
        writeNestedLine("intercept: $CSY.Interceptor,")
    }
    writeNestedLine("): ${service.withTypes} = object : ${service.withTypes} {", "}") {
        functions.forEachSeparator({ writeLine() }) { function ->
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
                        if (hasResult) write(" as ${function.returnType!!.toType()}") else writeLine()
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
                        function.parameters.forEachIndexed { parameterIndex, parameter ->
                            writeNestedLine("parameters[$parameterIndex] as ${parameter.type.toType()},")
                        }
                    }
                }
                writeNestedLine($$"else -> error(\"service '$id' has no function '$function'\")")
            }
        }
    }
}

private val AnyFunctions = setOf("toString", "equals", "hashCode")
