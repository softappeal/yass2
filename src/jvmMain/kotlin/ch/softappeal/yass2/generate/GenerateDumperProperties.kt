package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import kotlin.reflect.*

public fun generateDumperProperties(
    concreteClasses: List<KClass<*>>, name: String = "GeneratedDumperProperties"
): String = writer {
    require(concreteClasses.toSet().size == concreteClasses.size) { "duplicated concreteClass" }
    write("""
        @Suppress("UNCHECKED_CAST", "RedundantVisibilityModifier")
        public val $name: $CSY.DumperProperties = $CSY.dumperProperties(
    """)
    concreteClasses.withIndex().forEach { (classIndex, klass) ->
        write("""
            ${klass.qualifiedName}::class to listOf(
        """, 1)
        val properties = ReflectionDumperProperties(klass)
        properties.withIndex().forEach { (propertyIndex, property) ->
            write("""
                ${klass.qualifiedName}::${property.name} as kotlin.reflect.KProperty1<Any, Any?>${separator(propertyIndex, properties)}
            """, 2)
        }
        write("""
            )${separator(classIndex, concreteClasses)}
        """, 1)
    }
    write("""
        )
    """)
}
