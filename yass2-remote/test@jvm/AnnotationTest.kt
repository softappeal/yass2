package ch.softappeal.yass2.remote

import kotlinx.coroutines.test.runTest
import kotlin.reflect.full.findAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AnnotationTest {
    @Test
    fun test() = runTest {
        var annotation: TestAnnotation? = null
        val calculator = CalculatorImpl.proxy { function, _, invoke ->
            annotation = function.findAnnotation<TestAnnotation>()
            invoke()
        }
        assertEquals(5, calculator.add(2, 3))
        println(annotation)
        assertSame(TestAnnotation::class, annotation!!.annotationClass)

        annotation = null
        calculator.noParametersNoResult()
        assertNull(annotation)
    }
}
