package ch.softappeal.yass2.generate

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
    writeNestedLine("override ${if (function.isSuspend) "suspend " else ""}fun ${function.name}(") {
        function.valueParameters.forEachIndexed { parameterIndex, parameter ->
            writeNestedLine("p${parameterIndex + 1}: ${parameter.type.toPrintable()},")
        }
    }
    writeNested(")")
}

private fun KFunction<*>.parameters() = (1..valueParameters.size).joinToString(", ") { "p$it" }

private val KClass<*>.withTypeParameters get() = "<${typeParameters.joinToString { it.name }}>"
private val KClass<*>.withTypes get() = "$qualifiedName${if (typeParameters.isEmpty()) "" else withTypeParameters}"
private val KClass<*>.types get() = if (typeParameters.isEmpty()) "" else " $withTypeParameters"

public fun CodeWriter.generateProxy(service: KClass<*>) {
    require(service.java.isInterface) { "${service.qualifiedName} must be an interface" }

    val functions = service.memberFunctions
        .filter { it.javaMethod!!.declaringClass != Any::class.java }
        .sortedBy { it.name }
        .apply {
            val methodNames = map { it.name }
            require(methodNames.hasNoDuplicates()) {
                "interface ${service.qualifiedName} has overloaded methods ${methodNames.duplicates()}" // NOTE: support for overloading is not worth it, it's even not possible in JavaScript
            }
        }

    writeLine()
    writeNestedLine("public fun${service.types} ${service.withTypes}.proxy(") {
        if (functions.any { !it.isSuspend }) writeNestedLine("intercept: $CSY.Interceptor,")
        if (functions.any { it.isSuspend }) writeNestedLine("suspendIntercept: $CSY.SuspendInterceptor,")
    }
    writeNestedLine("): ${service.withTypes} = object : ${service.withTypes} {", "}") {
        functions.forEachIndexed { functionIndex, function ->
            if (functionIndex != 0) writeLine()
            val hasResult = function.hasResult()
            writeSignature(function)
            if (hasResult) write(": ${function.returnType.toPrintable()}")
            writeLine(" {") {
                writeNestedLine("${if (hasResult) "return " else ""}${if (function.isSuspend) "suspendIntercept" else "intercept"}(\"${function.name}\", listOf(${function.parameters()})) {") {
                    writeNestedLine("this@proxy.${function.name}(${function.parameters()})")
                }
                writeNested("}")
            }
            if (hasResult) write(" as ${function.returnType.toPrintable()}")
            writeLine()
            writeNestedLine("}")
        }
    }

    if (functions.any { !it.isSuspend }) return

    writeLine()
    writeNestedLine("public fun${service.types} ${ServiceId::class.qualifiedName}<${service.withTypes}>.proxy(") {
        writeNestedLine("tunnel: $CSY.remote.Tunnel,")
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
                        if (hasResult) write(" as ${function.returnType.toPrintable()}") else writeLine()
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
                            writeNestedLine("parameters[$parameterIndex] as ${parameter.type.toPrintable()},")
                        }
                    }
                }
                writeNestedLine("else -> error(\"service '${'$'}id' has no function '${'$'}function'\")")
            }
        }
    }
}
