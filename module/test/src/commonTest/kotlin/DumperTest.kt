package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.contract.generated.*
import ch.softappeal.yass2.serialize.binary.*
import kotlin.test.*

open class DumperTest {
    protected open val properties = GeneratedDumperProperties

    private fun dumper() = dumper(properties, StringBuilder::valueDumper, GraphConcreteClasses.toSet())

    @Test
    fun missingClass() {
        try {
            StringBuilder().(dumper())(Any())
        } catch (e: IllegalStateException) {
            println(e)
        }
    }

    @Test
    fun test() {
        val s = StringBuilder()
        val d = dumper()
        fun dump(value: Any?) {
            s.d(value).appendLine()
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
    Node(
        id = 1
        link = Node(
            id = 2
            link = Node(
                id = 3
                link = #1
            ) #2
        ) #1
    ) #0

""".trimIndent()
