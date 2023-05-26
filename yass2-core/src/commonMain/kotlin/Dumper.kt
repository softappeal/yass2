package ch.softappeal.yass2

import kotlin.reflect.*

public typealias DumperProperties = (type: KClass<*>) -> List<KProperty1<Any, Any?>>

public typealias Dumper = Appendable.(value: Any?) -> Appendable

/**
 * Supports the following types out-of-the-box:
 * `null`, [Boolean], [Number], [CharSequence], [List], [Enum] and classes with its properties.
 * [dumpValue] writes value (without line breaks) if responsible else does nothing.
 */
public fun createDumper(
    // TODO: generate properties and graphConcreteClasses into this
    properties: DumperProperties,
    graphConcreteClasses: Set<KClass<*>>,
    dumpValue: Appendable.(value: Any) -> Unit,
): Dumper = { value ->
    val dumperAppendable = this
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
            list.forEachIndexed { index, element ->
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
            inc("${type.simpleName}(${if (graph) " #$index" else ""}")
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
            is Boolean -> append(value.toString())
            is Number -> append(value.toString())
            is CharSequence -> append("\"$value\"")
            is Enum<*> -> append(value.name)
            is List<*> -> dumpList(value)
            else -> {
                var appended = false
                object : Appendable {
                    override fun append(value: Char): Appendable {
                        appended = true
                        dumperAppendable.append(value)
                        return this
                    }

                    override fun append(value: CharSequence?): Appendable {
                        appended = true
                        dumperAppendable.append(value)
                        return this
                    }

                    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
                        appended = true
                        dumperAppendable.append(value, startIndex, endIndex)
                        return this
                    }
                }.dumpValue(value)
                if (!appended) dumpObject(value)
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
