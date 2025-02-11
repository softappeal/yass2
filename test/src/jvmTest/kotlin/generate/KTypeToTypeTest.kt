package ch.softappeal.yass2.generate

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVariance
import kotlin.test.Test
import kotlin.test.assertEquals

// see https://youtrack.jetbrains.com/issue/KT-74925/kotlin.reflect.KType.toString-should-not-add-a-comment-for-Exception

/**
 * Own "implementation" of [KType.toString].
 * It has the following problems:
 * - `typealias` isn't handled correctly (see [KTypeToTypeTest.exceptionTest]).
 *   There isn't yet a reflection API for `typealias` (see [kotlin.reflect.jvm.internal.KTypeImpl.convert]
 *   and [KT-21489](https://youtrack.jetbrains.com/issue/KT-21489/Support-type-aliases-in-reflection)).
 * - [MutableList] isn't handled correctly (see [KTypeToTypeTest.mutableListTest]).
 *   [KClass.qualifiedName] gives wrongly `kotlin.collections.List`.
 *   There isn't yet a reflection API for handling this.
 *   See [KT-11754](https://youtrack.jetbrains.com/issue/KT-11754/Support-special-KClass-instances-for-mutable-collection-interfaces).
 */
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
            is KClass<*> -> append(c.qualifiedName).appendGenerics()
            is KTypeParameter -> append(c.name)
            else -> error("unexpected classifier '$c'")
        }
        if (isMarkedNullable) append('?')
    }
}

class KTypeToTypeTest {
    private fun mutableList() = mutableListOf(123)

    @Test
    fun mutableListTest() {
        assertEquals(
            "kotlin.collections.MutableList<kotlin.Int>",
            ::mutableList.returnType.toString()
        )
        assertEquals(
            "kotlin.collections.List<kotlin.Int>", // WRONG
            ::mutableList.returnType.toType()
        )
    }

    private fun exception() = Exception()

    @Test
    fun exceptionTest() {
        assertEquals(
            "kotlin.Exception /* = java.lang.Exception */", // WRONG
            ::exception.returnType.toString()
        )
        assertEquals(
            "java.lang.Exception", // WRONG
            ::exception.returnType.toType()
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
