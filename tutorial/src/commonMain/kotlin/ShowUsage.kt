package ch.softappeal.yass2.tutorial

import ch.softappeal.yass2.serialize.string.StringSerializer
import ch.softappeal.yass2.serialize.string.readString
import ch.softappeal.yass2.serialize.string.writeString

private val CalculatorImpl: Calculator = object : Calculator {
    override suspend fun add(a: Int, b: Int) = a + b
    override suspend fun divide(a: Int, b: Int) = if (b == 0) throw DivideByZeroException() else a / b
}

private suspend fun useCalculator(calculator: Calculator) {
    println("1 + 2 = ${calculator.add(1, 2)}")
}

private suspend fun showUsage() {
    fun useSerializer(serializer: StringSerializer) {
        println("*** useSerializer ***")
        val serialized = serializer.writeString(MyDate(123456))
        println(serialized)
        println(serializer.readString(serialized))
    }

    suspend fun useInterceptor() {
        println("*** useInterceptor ***")
        val calculator = CalculatorImpl.proxy { function, _, invoke ->
            println("calling function '$function'")
            invoke()
        }
        useCalculator(calculator)
    }
    useSerializer(TransportSerializer)
    useInterceptor()
}

public suspend fun main() {
    showUsage()
}
