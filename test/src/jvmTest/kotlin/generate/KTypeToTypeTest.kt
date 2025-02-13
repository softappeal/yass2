package ch.softappeal.yass2.generate

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVariance
import kotlin.test.Test
import kotlin.test.assertEquals

// Hack for: https://youtrack.jetbrains.com/issue/KT-11754/Support-special-KClass-instances-for-mutable-collection-interfaces -> create subinterfaces with subimplementation
// There isn't yet a reflection API for handling this.
interface IMutableList<E> : MutableList<E>

/** Must be used in  [ch.softappeal.yass2.serialize.binary.BinarySerializer.listEncoderId] */
class IArrayList<E>(initialCapacity: Int) : IMutableList<E>, ArrayList<E>(initialCapacity)

/** Doesn't work (see [KTypeToTypeTest.tMutableListTest]). */
typealias TMutableList<E> = MutableList<E>

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
    private fun mutableList() = mutableListOf(123)

    @Test
    fun mutableListTest() {
        assertEquals(
            "kotlin.collections.MutableList<kotlin.Int>",
            ::mutableList.returnType.toString(),
        )
        assertEquals(
            "kotlin.collections.List<kotlin.Int>", // WRONG
            ::mutableList.returnType.toType(),
        )
    }

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

    private fun iMutableList(): IMutableList<Int> = IArrayList<Int>(1).apply { add(123) }

    @Test
    fun iMutableListTest() {
        assertType(
            "ch.softappeal.yass2.generate.IMutableList<kotlin.Int>",
            ::iMutableList,
        )
        assertEquals(listOf(123, 321), iMutableList().apply { add(321) })
    }

    private fun tMutableList(): TMutableList<Int> = mutableListOf(123)

    @Test
    fun tMutableListTest() {
        assertEquals(
            "ch.softappeal.yass2.generate.TMutableList<kotlin.Int> /* = kotlin.collections.MutableList<kotlin.Int> */",
            ::tMutableList.returnType.toString(),
        )
        assertEquals(
            "kotlin.collections.List<kotlin.Int>", // WRONG
            ::tMutableList.returnType.toType(),
        )
        assertEquals(listOf(123, 321), tMutableList().apply { add(321) })
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
