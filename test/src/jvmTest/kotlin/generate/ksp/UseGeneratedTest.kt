package ch.softappeal.yass2.generate.ksp

import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.transport.copy
import kotlin.test.Test
import kotlin.test.assertEquals

@GenerateProxy
interface Adder {
    fun add(a: Int, b: Int): Int
}

private val AdderImpl = object : Adder {
    override fun add(a: Int, b: Int) = a + b
}

@GenerateBinarySerializer(
    baseEncoderClasses = [IntEncoder::class],
    enumClasses = [],
    treeConcreteClasses = [],
    graphConcreteClasses = [],
    withDumper = false,
)
private val ContractSerializer = createSerializer()

@GenerateDumper(
    treeConcreteClasses = [],
    graphConcreteClasses = [],
)
private val Dumper = createDumper {}

class UseGeneratedTest {
    @Test
    fun test() {
        assertEquals(1001, ContractSerializer.copy(1001))
        assertEquals("321", StringBuilder().Dumper(321).toString())
        val adder = AdderImpl.proxy { function, _, invoke ->
            println(function.name)
            invoke()
        }
        assertEquals(3, adder.add(1, 2))
    }
}
