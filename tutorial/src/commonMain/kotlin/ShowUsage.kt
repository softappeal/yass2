package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.core.remote.tunnel
import ch.softappeal.yass2.core.serialize.string.StringSerializer
import ch.softappeal.yass2.core.serialize.string.fromString
import ch.softappeal.yass2.core.serialize.string.toString

private object CalculatorImpl : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

private suspend fun useCalculator(calculator: Calculator) {
    println("1 + 2 = ${calculator.add(1, 2)}")
}

private fun useSerializer(serializer: StringSerializer) {
    println("*** useSerializer ***")
    val serialized = serializer.toString(MyDate(123456))
    println(serialized)
    println(serializer.fromString(serialized))
}

private suspend fun useInterceptor() {
    println("*** useInterceptor ***")
    val calculator = CalculatorImpl.proxy { function, _, invoke ->
        println("calling function '$function'")
        invoke()
    }
    useCalculator(calculator)
}

private suspend fun useRemoting() {
    println("*** useRemoting ***")
    val calculator = CalculatorId.proxy(tunnel(CalculatorId.service(CalculatorImpl)))
    useCalculator(calculator)
}

suspend fun main() {
    useSerializer(TutorialSerializer)
    useInterceptor()
    useRemoting()
}
