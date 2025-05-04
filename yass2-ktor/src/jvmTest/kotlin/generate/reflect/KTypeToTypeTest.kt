package ch.softappeal.yass2.generate.reflect

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVariance
import kotlin.test.Test
import kotlin.test.assertEquals

/** Own "implementation" of [KType.toString]. */
private fun KType.toType(): String {
    fun Appendable.appendGenerics() {
        if (arguments.isEmpty()) return
        append('<')
        arguments.forEachIndexed { argumentIndex, argument ->
            if (argumentIndex != 0) append(", ")
            when (argument.variance) {
                null -> append('*')
                KVariance.INVARIANT -> {}
                KVariance.IN -> append("in ")
                KVariance.OUT -> append("out ")
            }
            if (argument.type != null) append(argument.type!!.toType())
        }
        append('>')
    }
    return buildString {
        when (val c = classifier) {
            is KClass<*> -> append(
                when (val qualifiedName = c.qualifiedName) {
                    // Hack for: https://youtrack.jetbrains.com/issue/KT-21489/Support-type-aliases-in-reflection -> enumerate all relevant types.
                    // There isn't yet a reflection API for `typealias` (see [kotlin.reflect.jvm.internal.KTypeImpl.convert]).
                    // This fixes: https://youtrack.jetbrains.com/issue/KT-74925/kotlin.reflect.KType.toString-should-not-add-a-comment-for-Exception.
                    "java.lang.Exception" -> "kotlin.Exception"
                    else -> qualifiedName
                }
            ).appendGenerics()
            is KTypeParameter -> append(c.name)
            else -> error("unexpected classifier '$c'")
        }
        if (isMarkedNullable) append('?')
    }
}

class KTypeToTypeTest {
    private fun exception() = Exception()

    @Test
    fun exceptionTest() {
        assertEquals(
            "kotlin.Exception /* = java.lang.Exception */",
            ::exception.returnType.toString(),
        )
        assertEquals(
            "kotlin.Exception",
            ::exception.returnType.toType(),
        )
    }

    private fun list() = listOf(123)

    @Test
    fun listTest() {
        assertType(
            "kotlin.collections.List<kotlin.Int>",
            ::list,
        )
    }

    private class Complex<A, B> {
        fun action() = mapOf<Map<in A, *>?, Map<out B?, Int?>>()
    }

    @Test
    fun complexTest() {
        assertType(
            "kotlin.collections.Map<kotlin.collections.Map<in A, *>?, kotlin.collections.Map<out B?, kotlin.Int?>>",
            Complex<String, Double>::action,
        )
    }

    private fun assertType(string: String, callable: KCallable<*>) {
        val type = callable.returnType
        assertEquals(string, type.toString())
        assertEquals(string, type.toType())
    }
}
