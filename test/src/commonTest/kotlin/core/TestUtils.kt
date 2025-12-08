package ch.softappeal.yass2.core

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

inline fun <reified E : Exception> assertFailsWithMessage(expectedMessage: String, block: () -> Unit) =
    assertEquals(expectedMessage, assertFailsWith(E::class, block).message)
