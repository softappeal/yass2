package ch.softappeal.yass2.generate

import com.google.devtools.ksp.symbol.*

internal fun Appendable.generateDumper(treeConcreteClasses: List<KSType>, graphConcreteClasses: List<KSType>) {
    val concreteClasses = treeConcreteClasses + graphConcreteClasses
    write("""

        private val DumperProperties = $CSY.dumperProperties(
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

        public fun createDumper(dumpValue: kotlin.text.Appendable.(value: kotlin.Any) -> kotlin.Unit): $CSY.Dumper = $CSY.createDumper(
            DumperProperties,
            setOf(
    """)
    graphConcreteClasses.forEach { appendLine("        ${it.declaration.name()}::class,") }
    write("""
            ),
            dumpValue,
        )
    """)
}
