package ch.softappeal.yass2.serialize.text

import kotlin.test.Test
import kotlin.test.assertFails

class JvmTextSerializerTest {
    @Test
    fun hackedList() { // TODO: seems to work only for jvm and linux
        println(assertFails {
            SERIALIZER.fromString(
                """
                    Example(
                        int: 10
                        any: Int(11)
                        list: [
                            A()
                        ]
                    )
                """.trimIndent()
            )
        })
    }
}
