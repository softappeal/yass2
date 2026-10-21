package ch.softappeal.yass2.remote

class DivideByZeroException : ContractException()

annotation class TestAnnotation

interface AddCalculator {
    @TestAnnotation
    suspend fun add(a: Int, b: Int): Int
}

interface Calculator : AddCalculator {
    suspend fun divide(a: Int, b: Int): Int
    suspend fun noParametersNoResult()
    suspend fun delay(milliSeconds: Int)
}

val CalculatorId = ServiceId<Calculator>("Calculator")

@Proxies(
    Calculator::class,
)
internal class Generate
