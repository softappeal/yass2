// TODO: comment out the following line for testing duplicated annotation
//@file:GenerateBinarySerializerAndDumper(baseEncoderClasses = [], treeConcreteClasses = [])

package ch.softappeal.yass2.serialize.binary

import ch.softappeal.yass2.*
import kotlin.test.*

class DuplicatedAnnotationTest {
    @Test
    fun neededForImport() {
        println(GenerateProxy::class)
    }
}
