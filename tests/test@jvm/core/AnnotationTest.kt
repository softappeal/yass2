package ch.softappeal.yass2.core

import ch.softappeal.yass2.TestAnnotation
import ch.softappeal.yass2.proxy
import kotlinx.coroutines.test.runTest
import kotlin.reflect.full.hasAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnnotationTest {
    @Test
    fun test() = runTest {
        var hasAnnotation = false
        val calculator = CalculatorImpl.proxy { function, _, invoke ->
            hasAnnotation = function.hasAnnotation<TestAnnotation>()
            invoke()
        }
        assertEquals(3, calculator.add(1, 2))
        assertTrue(hasAnnotation)
        calculator.noParametersNoResult()
        assertFalse(hasAnnotation)
    }
}
