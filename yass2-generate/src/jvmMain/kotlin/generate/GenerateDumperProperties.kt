package ch.softappeal.yass2.generate

import ch.softappeal.yass2.reflect.*
import kotlin.reflect.*

public const val DUMPER_PROPERTIES: String = "DumperProperties"

public fun generateDumperProperties(
    concreteClasses: List<KClass<*>>,
    name: String = GENERATED + DUMPER_PROPERTIES,
): String = writer {
    require(concreteClasses.toSet().size == concreteClasses.size) { "duplicated concreteClass" }
    write("""
        @Suppress("RedundantSuppression", "UNCHECKED_CAST", "RedundantVisibilityModifier")
        public val ${name.firstCharToUppercase()}: $CSY.DumperProperties = $CSY.dumperProperties(
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
