package ch.softappeal.yass2.serialize.text

import ch.softappeal.yass2.assertFailsMessage
import ch.softappeal.yass2.contract.A
import ch.softappeal.yass2.contract.B
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Gender
import ch.softappeal.yass2.contract.GenderWrapper
import ch.softappeal.yass2.contract.IntException
import ch.softappeal.yass2.contract.IntWrapper
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.Optionals
import ch.softappeal.yass2.contract.Poly
import ch.softappeal.yass2.contract.ThrowableFake
import ch.softappeal.yass2.contract.reflect.createTextSerializer
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.binary.ManyPropertiesConst
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val SERIALIZER = createTextSerializer()

private fun dump(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, SERIALIZER.writeString(data))
    }
    write(value)
    write(SERIALIZER.readString(serialized))
    others.forEach { write(SERIALIZER.readString(it)) }
}

private class Int
private class MyIntEncoder : TextEncoder<Int>(Int::class, { }, { Int() })

class TextSerializerTest {
    @Test
    fun test() {
        dump(
            GenderWrapper(Gender.Male),
            "GenderWrapper(gender:Male)",
        )
        dump(
            Poly(A(1), B(3, 4)),
            "Poly(a:A(a:1),b:B(a:3,b:4))",
        )
        dump(
            Poly(B(1, 2), B(3, 4)),
            "Poly(a:B(a:1,b:2),b:B(a:3,b:4))",
            "Poly(a:B(a:1,b:2,),b:B(a:3,b:4,),)",
            "Poly(a:B(a:1,b:2)b:B(a:3,b:4))",
        )
        dump(
            listOf(null, 123, A(1), mutableListOf(1, 2)),
            "[*,Int(123),A(a:1),[Int(1),Int(2)]]",
            "[*,Int(123),A(a:1,),[Int(1),Int(2),],]",
            "[*Int(123)A(a:1)[Int(1)Int(2)]]",
        )
        dump(
            Optionals(1, null, IntWrapper(3), null),
            "Optionals(i:1,intWrapper:IntWrapper(i:3))",
        )
        dump(
            Optionals(1, 2, IntWrapper(3), IntWrapper(4)),
            "Optionals(i:1,iOptional:2,intWrapper:IntWrapper(i:3),intWrapperOptional:IntWrapper(i:4))",
        )
        dump(
            Lists(listOf(1, 2), listOf(3, 4), mutableListOf(5, 6), mutableListOf(7, 8)),
            "Lists(list:[Int(1),Int(2)],listOptional:[Int(3),Int(4)],mutableList:[Int(5),Int(6)],mutableListOptional:[Int(7),Int(8)])",
        )
        dump(
            Lists(listOf(1, 2), null, mutableListOf(3, 4), null),
            "Lists(list:[Int(1),Int(2)],mutableList:[Int(3),Int(4)])",
        )
        dump(
            IntWrapper(3),
            "IntWrapper(i:3)",
        )
        dump(
            ThrowableFake("hello", "world"),
            "ThrowableFake(cause:\"hello\",message:\"world\")",
        )
        dump(
            ThrowableFake(null, "m"),
            "ThrowableFake(message:\"m\")",
        )
        dump(
            ManyPropertiesConst,
            "ManyProperties(h:8,d:4,f:6,g:7,b:2,a:1,c:3,e:5,i:9,j:10)",
        )
        dump(
            IntException(10),
            "IntException(i:10)",
        )
        dump(
            DivideByZeroException(),
            "DivideByZeroException()",
        )
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> {
            object : TextSerializer() {
                init {
                    initialize(IntTextEncoder(), IntTextEncoder())
                }
            }
        }.message!!
        assertTrue(message.startsWith("duplicated type 'class "))
        assertTrue(message.endsWith("Int'"))
    }

    @Test
    fun duplicatedClassName() {
        assertFailsMessage<IllegalArgumentException>("duplicated className 'Int'") {
            object : TextSerializer() {
                init {
                    initialize(IntTextEncoder(), MyIntEncoder())
                }
            }
        }
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> {
            SERIALIZER.write(BytesWriter(1000), true)
        }.message!!
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("Boolean'"))
    }

    @Test
    fun missingEncoder() {
        assertFailsMessage<IllegalStateException>("missing encoder for class 'X'") {
            SERIALIZER.readString("X()")
        }
    }

    @Test
    fun properties() {
        SERIALIZER.readString("Optionals(i:1,intWrapper:IntWrapper(i:3))") // implicit optional properties iOptional and intWrapperOptional
        assertNull( // optional property intWrapperOptional with id explicitly set to null
            (SERIALIZER.readString("Optionals(i:1,intWrapper:IntWrapper(i:3),intWrapperOptional:*)") as Optionals).intWrapperOptional
        )
        assertFailsMessage<IllegalStateException>("duplicated property 'i' for type 'Optionals'") {
            SERIALIZER.readString("Optionals(i:1,intWrapper:IntWrapper(i:3),i:2)")
        }
        println(assertFailsWith<Exception> { // wrong typ of property intWrapper
            SERIALIZER.readString("Optionals(i:1,intWrapper:A(a:3))")
        })
        println(assertFailsWith<Exception> { // required property intWrapper set to null
            SERIALIZER.readString("Optionals(i:1,intWrapper:*)")
        })
        println(assertFailsWith<Exception> { // missing required property intWrapper
            SERIALIZER.readString("Optionals(i:1)")
        })
        println(assertFailsWith<Exception> { // missing required property i
            SERIALIZER.readString("Optionals(intWrapper:IntWrapper(i:3))")
        })
        SERIALIZER.readString("Optionals(i:1,intWrapper:IntWrapper(i:3),ignored:*)") // TODO: illegal properties are ignored
    }
}
