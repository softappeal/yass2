package ch.softappeal.yass2.generate

import kotlin.reflect.*
import kotlin.reflect.full.*

private fun KClass<*>.properties(): List<KProperty1<Any, Any?>> = memberProperties
    .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
    .sortedBy { it.name }
    .map { @Suppress("UNCHECKED_CAST") (it as KProperty1<Any, Any?>) }

public fun generateDumperProperties(
    concreteClasses: List<KClass<*>>,
    name: String = "generatedDumperProperties",
): String = writer {
    require(concreteClasses.toSet().size == concreteClasses.size) { "duplicated concreteClass" }
    // NOTE: is 'fun' instead 'val' due to Kotlin Native/JS global variables initialize order bug
    write("""
        @Suppress("UNCHECKED_CAST", "RedundantVisibilityModifier")
        public fun $name(): $CSY.DumperProperties = $CSY.dumperProperties(
    """)
    concreteClasses.forEach { klass ->
        write("""
            ${klass.qualifiedName}::class to listOf(
        """, 1)
        klass.properties().forEach { property ->
            write("""
                ${klass.qualifiedName}::${property.name} as kotlin.reflect.KProperty1<Any, Any?>,
            """, 2)
        }
        write("""
            ),
        """, 1)
    }
    write("""
        )
    """)
}
