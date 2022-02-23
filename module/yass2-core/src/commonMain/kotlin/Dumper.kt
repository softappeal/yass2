package ch.softappeal.yass2

import kotlin.reflect.*

public typealias DumperProperties = (type: KClass<*>) -> List<KProperty1<Any, Any?>>

public typealias Dumper = StringBuilder.(value: Any?) -> StringBuilder

/**
 * Supports the following types out-of-the-box:
 * `null`, [Boolean], [Number], [CharSequence], [List] and classes with its properties.
 * [valueDumper] writes value (without line breaks) if responsible else does nothing.
 */
public fun dumper(
    @UnspecifiedInitializationOrder(workaround = "supplier") propertiesSupplier: () -> DumperProperties,
    valueDumper: StringBuilder.(value: Any) -> Unit,
): Dumper {
    val properties = propertiesSupplier()
    return { value ->
        var indent = 0

        fun dump(value: Any?) {
            fun appendIndent() {
                append("    ".repeat(indent))
            }

            fun inc(s: CharSequence) {
                append(s)
                appendLine()
                indent++
            }

            fun dec(s: CharSequence) {
                indent--
                appendIndent()
                append(s)
            }

            fun dumpList(list: List<*>) {
                inc("[")
                for (element in list) {
                    appendIndent()
                    dump(element)
                    appendLine()
                }
                dec("]")
            }

            fun dumpObject(obj: Any) {
                val type = obj::class
                inc("${type.simpleName}(")
                for (property in properties(type)) property.get(obj)?.let { propertyValue ->
                    appendIndent()
                    append("${property.name} = ")
                    dump(propertyValue)
                    appendLine()
                }
                dec(")")
            }

            when (value) {
                null -> append("null")
                is Boolean -> append(value)
                is Number -> append(value)
                is CharSequence -> append("\"$value\"")
                is List<*> -> dumpList(value)
                else -> {
                    val oldLength = length
                    valueDumper(value)
                    if (oldLength == length) dumpObject(value)
                }
            }
        }

        dump(value)
        this
    }
}

public fun dumperProperties(vararg mappings: Pair<KClass<*>, List<KProperty1<Any, Any?>>>): DumperProperties {
    val map = mapOf(*mappings)
    return { type -> map[type] ?: error("missing mapping for '$type'") }
}
