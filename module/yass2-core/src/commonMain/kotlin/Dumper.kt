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
    properties: DumperProperties,
    valueDumper: StringBuilder.(value: Any) -> Unit,
    graphConcreteClasses: Set<KClass<*>> = emptySet(),
): Dumper = { value ->
    val object2reference: HashMap<Any, Int> by lazy(LazyThreadSafetyMode.NONE) { HashMap(16) }
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
            for ((index, element) in list.withIndex()) {
                appendIndent()
                append("$index: ")
                dump(element)
                appendLine()
            }
            dec("]")
        }

        fun dumpObject(obj: Any) {
            val type = obj::class
            val graph = type in graphConcreteClasses
            val index = if (graph) {
                val reference = object2reference[obj]
                if (reference != null) {
                    append("#$reference")
                    return
                }
                object2reference.size.apply { object2reference[obj] = this }
            } else 0
            inc("${type.simpleName}(")
            for (property in properties(type)) property.get(obj)?.let { propertyValue ->
                appendIndent()
                append("${property.name} = ")
                dump(propertyValue)
                appendLine()
            }
            dec(")")
            if (graph) append(" #$index")
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

public fun dumperProperties(vararg mappings: Pair<KClass<*>, List<KProperty1<Any, Any?>>>): DumperProperties {
    val map = mapOf(*mappings)
    return { type -> map[type] ?: error("missing mapping for '$type'") }
}
