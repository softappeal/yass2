package ch.softappeal.yass2.serialize.text // TODO: add tests for package

import ch.softappeal.yass2.contract.A
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.GenderWrapper
import ch.softappeal.yass2.contract.IntWrapper
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.Optionals
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.reflect.createTextSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

private val SERIALIZER = createTextSerializer()

private fun dump(value: Any?, serialized: String) {
    fun write(data: Any?) {
        assertEquals(serialized, SERIALIZER.writeString(data))
    }
    write(value)
    write(SERIALIZER.readString(serialized))
}

class TextSerializerTest {
    @Test
    fun test() {
        dump(
            null,
            "*",
        )
        dump(
            Gender.Male,
            "Gender(Male)",
        )
        dump(
            GenderWrapper(Gender.Male),
            "GenderWrapper(gender=Male)",
        )
        dump(
            Poly(A(1), B(3, 4)),
            "Poly(a=A(a=1),b=B(a=3,b=4))",
        )
        dump(
            Poly(B(1, 2), B(3, 4)),
            "Poly(a=B(a=1,b=2),b=B(a=3,b=4))",
        )
        dump(
            listOf(null, 123, A(1), mutableListOf(1, 2)),
            "[*,Int(123),A(a=1),[Int(1),Int(2)]]",
        )
        dump(
            Optionals(1, null, IntWrapper(3), null),
            "Optionals(i=1,intWrapper=IntWrapper(i=3))",
        )
        dump(
            Optionals(1, 2, IntWrapper(3), IntWrapper(4)),
            "Optionals(i=1,iOptional=2,intWrapper=IntWrapper(i=3),intWrapperOptional=IntWrapper(i=4))",
        )
        dump(
            Lists(listOf(1, 2), listOf(3, 4), mutableListOf(5, 6), mutableListOf(7, 8)),
            "Lists(list=[Int(1),Int(2)],listOptional=[Int(3),Int(4)],mutableList=[Int(5),Int(6)],mutableListOptional=[Int(7),Int(8)])",
        )
        dump(
            Lists(listOf(1, 2), null, mutableListOf(3, 4), null),
            "Lists(list=[Int(1),Int(2)],mutableList=[Int(3),Int(4)])",
        )
        dump(
            123,
            "Int(123)",
        )
        dump(
            Gender.Male,
            "Gender(Male)",
        )
        dump(
            listOf<Int>(),
            "[]",
        )
        dump(
            listOf(null),
            "[*]",
        )
        dump(
            listOf(null, null),
            "[*,*]",
        )
        dump(
            listOf(null, listOf(null, null, null), null),
            "[*,[*,*,*],*]",
        )
        dump(
            IntWrapper(3),
            "IntWrapper(i=3)",
        )
        dump(
            "hello",
            "\"hello\""
        )
        dump(
            listOf("hello"),
            "[\"hello\"]",
        )
        dump(
            ThrowableFake("hello", "world"),
            "ThrowableFake(cause=\"hello\",message=\"world\")",
        )
        dump(
            ThrowableFake(null, "m"),
            "ThrowableFake(message=\"m\")",
        )
    }
}
