package ch.softappeal.yass2

import kotlin.reflect.*

public typealias DumperProperties = (type: KClass<*>) -> List<KProperty1<Any, Any?>>

/** Writes value (without line breaks) if responsible else does nothing. */
public typealias ValueDumper = StringBuilder.(value: Any) -> Unit

public typealias Dumper = StringBuilder.(value: Any?) -> StringBuilder

/**
 * Supports the following types out-of-the-box:
 * `null`, [Boolean], [Number], [CharSequence], [List] and classes with its properties.
 */
public fun dumper(
    properties: DumperProperties,
    valueDumper: ValueDumper,
    compact: Boolean = true,
    graphConcreteClasses: Set<KClass<*>> = emptySet()
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
            if (compact) {
                append('[')
                var first = true
                for (element in list) {
                    if (first) first = false else append(',')
                    dump(element)
                }
                append(']')
            } else {
                inc("[")
                for (element in list) {
                    appendIndent()
                    dump(element)
                    appendLine()
                }
                dec("]")
            }
        }

        fun dumpObject(obj: Any) {
            val type = obj::class
            val graph = type in graphConcreteClasses
            val index: Int
            if (graph) {
                val reference = object2reference[obj]
                if (reference != null) {
                    append("#$reference")
                    return
                }
                index = object2reference.size
                object2reference[obj] = index
            } else {
                index = 0
            }
            if (compact) {
                append("${type.simpleName}(")
                var first = true
                for (property in properties(type)) property.get(obj)?.let { propertyValue ->
                    if (first) first = false else append(',')
                    append("${property.name}=")
                    dump(propertyValue)
                }
            } else {
                inc("${type.simpleName}(")
                for (property in properties(type)) property.get(obj)?.let { propertyValue ->
                    appendIndent()
                    append("${property.name} = ")
                    dump(propertyValue)
                    appendLine()
                }
                dec("")
            }
            append(")${if (graph) "#$index" else ""}")
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
