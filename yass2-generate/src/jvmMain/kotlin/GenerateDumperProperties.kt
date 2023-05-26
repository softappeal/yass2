package ch.softappeal.yass2.generate

import com.google.devtools.ksp.symbol.*

internal fun Appendable.generateDumperProperties(concreteClasses: List<KSType>) {
    write("""

        public val GeneratedDumperProperties: $CSY.DumperProperties = $CSY.dumperProperties(
    """)
    concreteClasses.forEach { klass ->
        write("""
            ${klass.declaration.name()}::class to listOf(
        """, 1)
        (klass.declaration as KSClassDeclaration).getAllProperties().toList().filterNot { it.isPropertyOfThrowable() }.sortedBy { it.toString() }.forEach { property ->
            write("""
                ${klass.declaration.name()}::${property} as kotlin.reflect.KProperty1<Any, Any?>,
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
