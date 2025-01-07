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
import ch.softappeal.yass2.contract.createTextSerializer
import ch.softappeal.yass2.serialize.BytesWriter
import ch.softappeal.yass2.serialize.binary.ManyPropertiesConst
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val SERIALIZER_SL = createTextSerializer(false)
private val SERIALIZER_ML = createTextSerializer(true)

private fun dumpSL(value: Any?, serialized: String, vararg others: String) {
    fun write(data: Any?) {
        assertEquals(serialized, SERIALIZER_SL.writeString(data))
    }
    write(value)
    write(SERIALIZER_SL.readString(serialized))
    others.forEach { write(SERIALIZER_SL.readString(it)) }
}

private fun dumpML(value: Any?, serialized: String) {
    fun write(data: Any?) {
        assertEquals(serialized, SERIALIZER_ML.writeString(data))
    }
    write(value)
    write(SERIALIZER_ML.readString(serialized))
}

private class Int
private class MyIntEncoder : TextEncoder<Int>(Int::class, { }, { Int() })

class TextSerializerTest {
    @Test
    fun sl() {
        dumpSL(
            Gender.Male,
            "Gender(Male)",
            "  Gender  (  Male  )",
        )
        dumpSL(
            123,
            "Int(123)",
            "  Int  (  123  )",
        )
        dumpSL(
            GenderWrapper(Gender.Male),
            "GenderWrapper(gender:Male)",
        )
        dumpSL(
            Poly(A(1), B(3, 4)),
            "Poly(a:A(a:1),b:B(a:3,b:4))",
        )
        dumpSL(
            Poly(B(1, 2), B(3, 4)),
            "Poly(a:B(a:1,b:2),b:B(a:3,b:4))",
            "Poly(a:B(a:1,b:2,),b:B(a:3,b:4,),)",
            "Poly(a:B(a:1,b:2)b:B(a:3,b:4))",
            "  Poly  (  a  :  B  (  a  :  1  b  :  2  )  b  :  B  (  a  :  3  b  :  4  )  )",
            "  Poly  (  a  :  B  (  a  :  1  ,  b  :  2  ,  )  ,  b  :  B  (  a  :  3  ,  b  :  4  ,  )  ,  )",
        )
        dumpSL(
            listOf(null, 123, A(1), mutableListOf(1, 2)),
            "[*,Int(123),A(a:1),[Int(1),Int(2)]]",
            "[*,Int(123),A(a:1,),[Int(1),Int(2),],]",
            "[*Int(123)A(a:1)[Int(1)Int(2)]]",
            "  \t \r \n [  *  Int  (  123  )  A  (  a  :  1  )  [  Int  (  1  )  Int  (  2  )  ]  ]",
            "  \t \r \n [  *  Int  (  123  )  A  (  a  :  1  )  [  Int  (  1  )  ,  Int  (  2  )  ,  ]  ,  ]",
        )
        dumpSL(
            Optionals(1, null, IntWrapper(3), null),
            "Optionals(i:1,intWrapper:IntWrapper(i:3))",
        )
        dumpSL(
            Optionals(1, 2, IntWrapper(3), IntWrapper(4)),
            "Optionals(i:1,iOptional:2,intWrapper:IntWrapper(i:3),intWrapperOptional:IntWrapper(i:4))",
        )
        dumpSL(
            Lists(listOf(1, 2), listOf(3, 4), mutableListOf(5, 6), mutableListOf(7, 8)),
            "Lists(list:[Int(1),Int(2)],listOptional:[Int(3),Int(4)],mutableList:[Int(5),Int(6)],mutableListOptional:[Int(7),Int(8)])",
        )
        dumpSL(
            Lists(listOf(1, 2), null, mutableListOf(3, 4), null),
            "Lists(list:[Int(1),Int(2)],mutableList:[Int(3),Int(4)])",
        )
        dumpSL(
            IntWrapper(3),
            "IntWrapper(i:3)",
        )
        dumpSL(
            ThrowableFake("hello", "world"),
            "ThrowableFake(cause:\"hello\",message:\"world\")",
        )
        dumpSL(
            ThrowableFake(null, "m"),
            "ThrowableFake(message:\"m\")",
        )
        dumpSL(
            ManyPropertiesConst,
            "ManyProperties(h:8,d:4,f:6,g:7,b:2,a:1,c:3,e:5,i:9,j:10)",
        )
        dumpSL(
            IntException(10),
            "IntException(i:10)",
        )
        dumpSL(
            DivideByZeroException(),
            "DivideByZeroException()",
        )
        dumpSL(listOf<kotlin.Int>(), "[]")
        dumpSL(listOf(null), "[*]")
        dumpSL(listOf(null, null), "[*,*]")
    }

    @Test
    fun ml() {
        dumpML(
            null,
            "*",
        )
        dumpML(
            Gender.Male,
            "Gender(Male)",
        )
        dumpML(
            123,
            "Int(123)",
        )
        dumpML(
            listOf(null, 123, A(1), mutableListOf(1, 2)),
            """
                [
                    *
                    Int(123)
                    A(
                        a: 1
                    )
                    [
                        Int(1)
                        Int(2)
                    ]
                ]
            """.trimIndent(),
        )
        dumpML(
            Poly(B(1, 2), B(3, 4)),
            """
                Poly(
                    a: B(
                        a: 1
                        b: 2
                    )
                    b: B(
                        a: 3
                        b: 4
                    )
                )
            """.trimIndent(),
        )
        dumpML(
            Lists(listOf(1, 2), null, mutableListOf(3, 4), null),
            """
                Lists(
                    list: [
                        Int(1)
                        Int(2)
                    ]
                    mutableList: [
                        Int(3)
                        Int(4)
                    ]
                )
            """.trimIndent(),
        )
        dumpML(
            DivideByZeroException(),
            """
                DivideByZeroException(
                )
            """.trimIndent(),
        )
        dumpML(
            listOf<kotlin.Int>(),
            """
                [
                ]
            """.trimIndent(),
        )
        dumpML(
            listOf(null),
            """
                [
                    *
                ]
            """.trimIndent(),
        )
        dumpML(
            listOf(null, null),
            """
                [
                    *
                    *
                ]
            """.trimIndent(),
        )
    }

    @Test
    fun duplicatedType() {
        val message = assertFailsWith<IllegalArgumentException> {
            object : TextSerializer(false) {
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
            object : TextSerializer(false) {
                init {
                    initialize(IntTextEncoder(), MyIntEncoder())
                }
            }
        }
    }

    @Test
    fun missingType() {
        val message = assertFailsWith<IllegalStateException> {
            SERIALIZER_SL.write(BytesWriter(1000), true)
        }.message!!
        assertTrue(message.startsWith("missing type 'class "))
        assertTrue(message.endsWith("Boolean'"))
    }

    @Test
    fun missingEncoder() {
        assertFailsMessage<IllegalStateException>("missing encoder for class 'X'") {
            SERIALIZER_SL.readString("X()")
        }
    }

    @Test
    fun properties() {
        SERIALIZER_SL.readString("Optionals(i:1,intWrapper:IntWrapper(i:3))") // implicit optional properties iOptional and intWrapperOptional
        assertNull( // optional property intWrapperOptional with id explicitly set to null
            (SERIALIZER_SL.readString("Optionals(i:1,intWrapper:IntWrapper(i:3),intWrapperOptional:*)") as Optionals).intWrapperOptional
        )
        assertFailsMessage<IllegalStateException>("duplicated property 'i' for type 'Optionals'") {
            SERIALIZER_SL.readString("Optionals(i:1,intWrapper:IntWrapper(i:3),i:2)")
        }
        println(assertFailsWith<Exception> { // wrong typ of property intWrapper
            SERIALIZER_SL.readString("Optionals(i:1,intWrapper:A(a:3))")
        })
        println(assertFailsWith<Exception> { // required property intWrapper set to null
            SERIALIZER_SL.readString("Optionals(i:1,intWrapper:*)")
        })
        println(assertFailsWith<Exception> { // missing required property intWrapper
            SERIALIZER_SL.readString("Optionals(i:1)")
        })
        println(assertFailsWith<Exception> { // missing required property i
            SERIALIZER_SL.readString("Optionals(intWrapper:IntWrapper(i:3))")
        })
        SERIALIZER_SL.readString("Optionals(i:1,intWrapper:IntWrapper(i:3),ignored:*)") // TODO: illegal properties are ignored
    }
}
