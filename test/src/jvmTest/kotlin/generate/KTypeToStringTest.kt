package ch.softappeal.yass2.generate

import kotlin.test.Test
import kotlin.test.assertEquals

// https://youtrack.jetbrains.com/issue/KT-74925/kotlin.reflect.KType.toString-SHOULD-NOT-add-a-comment-for-Exception

/**
 * [kotlin.reflect.KType.toString] SHOULD NOT add a comment for [Exception].
 *
 * This is unfortunate if used in code generation.
 * See below as an example.
 * The signature is generated with [kotlin.reflect.KType.toString].
 * The problem is the line
 * `exception: kotlin.Exception /* = java.lang.Exception */,`.
 * - It gives a `TrailingComma` warning and worse
 * - reformatting moves the comma to
 *   `exception: kotlin.Exception, /* = java.lang.Exception */`
 *   which changes a file under version control.
 */
@Suppress("TrailingComma", "RemoveRedundantQualifierName", "unused")
private fun generatedCode(
    string: kotlin.String,
    list: kotlin.collections.List<kotlin.Int>,
    exception: kotlin.Exception /* = java.lang.Exception */,
    boolean: kotlin.Boolean,
) {
    println("$string $list $exception $boolean")
}

private fun string() = String()
private fun list() = listOf(123)
private fun exception() = Exception()

class KTypeToStringTest {
    @Test
    fun test() {
        // OK, no comment
        assertEquals(
            "kotlin.String",
            ::string.returnType.toString()
        )

        // OK, no comment
        assertEquals(
            "kotlin.collections.List<kotlin.Int>",
            ::list.returnType.toString()
        )

        // I would prefer just "kotlin.Exception".
        // Why is only in this case a comment?
        assertEquals(
            "kotlin.Exception /* = java.lang.Exception */",
            ::exception.returnType.toString()
        )
    }
}
