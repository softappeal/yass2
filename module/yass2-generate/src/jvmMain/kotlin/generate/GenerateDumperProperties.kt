package ch.softappeal.yass2.generate

import ch.softappeal.yass2.*
import ch.softappeal.yass2.reflect.*
import kotlin.reflect.*

public fun generateDumperProperties(
    concreteClasses: List<KClass<*>>,
    name: String = "generatedDumperProperties",
): String = writer {
    require(concreteClasses.toSet().size == concreteClasses.size) { "duplicated concreteClass" }
    @UnspecifiedInitializationOrder(workaround = "'fun' instead 'val'")
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
