package ch.softappeal.yass2

import ch.softappeal.yass2.contract.ComplexId
import ch.softappeal.yass2.contract.DivideByZeroException
import ch.softappeal.yass2.contract.Dumper
import ch.softappeal.yass2.contract.Lists
import ch.softappeal.yass2.contract.PlainId
import ch.softappeal.yass2.contract.child.ChildDumper
import ch.softappeal.yass2.serialize.binary.ManyPropertiesConst
import ch.softappeal.yass2.serialize.binary.createGraph
import kotlin.test.Test
import kotlin.test.assertEquals

enum class Color { Green }

class DumperTest {
    @Test
    fun missingClass() {
        try {
            StringBuilder().Dumper(Any())
        } catch (e: IllegalStateException) {
            println(e)
        }
    }

    @Test
    fun test() {
        val s = StringBuilder()
        fun dump(value: Any?) {
            s.Dumper(value).appendLine()
        }
        dump(null)
        dump(true)
        dump(false)
        dump(123.toByte())
        dump(123)
        dump(123.toLong())
        dump(1.23)
        dump(byteArrayOf(1, 2, 3))
        dump("hello")
        dump(Color.Green)
        dump(emptyList<Int>())
        dump(listOf(null, 123))
        dump(ManyPropertiesConst)
        dump(ComplexId(plainIdOptional = PlainId(61)))
        dump(ComplexId())
        dump(Lists(list = listOf(PlainId(), PlainId()), mutableList = mutableListOf(PlainId(61))))
        dump(DivideByZeroException())
        dump(createGraph())
        println(">$s<")
        assertEquals(Output, s.toString())
    }

    @Test
    fun childTest() {
        val s = StringBuilder()
        fun dump(value: Any?) {
            s.ChildDumper(value).appendLine()
        }
        dump(ManyPropertiesConst)
        dump(createGraph())
        println(">$s<")
        assertEquals(ChildOutput, s.toString())
    }
}

private val Output = """
    null
    true
    false
    123
    123
    123
    1.23
    binary
    "hello"
    Green
    [
    ]
    [
        0: null
        1: 123
    ]
    ManyProperties(
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
    )
    ComplexId(
        baseId = PlainId(
            id = 60
        )
        id = 58
        plainId = PlainId(
            id = 59
        )
        plainIdOptional = PlainId(
            id = 61
        )
    )
    ComplexId(
        baseId = PlainId(
            id = 60
        )
        id = 58
        plainId = PlainId(
            id = 59
        )
    )
    Lists(
        list = [
            0: PlainId(
                id = 60
            )
            1: PlainId(
                id = 60
            )
        ]
        mutableList = [
            0: PlainId(
                id = 61
            )
        ]
    )
    DivideByZeroException(
    )
    Node( #0
        id = 1
        link = Node( #1
            id = 2
            link = Node( #2
                id = 3
                link = #1
            )
        )
    )

""".trimIndent()

private val ChildOutput = """
    ManyProperties(
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
    )
    Node( #0
        id = 1
        link = Node( #1
            id = 2
            link = Node( #2
                id = 3
                link = #1
            )
        )
    )

""".trimIndent()
