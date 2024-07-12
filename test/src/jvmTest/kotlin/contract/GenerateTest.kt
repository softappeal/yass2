package ch.softappeal.yass2.contract

import ch.softappeal.yass2.contract.child.NoSuspend
import ch.softappeal.yass2.generate.Mode
import ch.softappeal.yass2.generate.generate
import ch.softappeal.yass2.generate.generateBinarySerializerAndDumper
import ch.softappeal.yass2.generate.generateDumper
import ch.softappeal.yass2.generate.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonTest/kotlin/contract/child",
            "ch.softappeal.yass2.contract.child",
            Mode.Verify,
        ) {
            generateProxy(NoSuspend::class)
            generateDumper(listOf(ManyProperties::class), listOf(Node::class))
        }
        generate(
            "src/commonTest/kotlin/contract",
            "ch.softappeal.yass2.contract",
            Mode.Verify,
        ) {
            generateProxy(Mixed::class)
            generateProxy(Calculator::class)
            generateProxy(Echo::class)
            generateBinarySerializerAndDumper(
                BaseEncoders,
                treeConcreteClasses = listOf(
                    IntException::class,
                    PlainId::class,
                    ComplexId::class,
                    Lists::class,
                    Id2::class,
                    Id3::class,
                    IdWrapper::class,
                    ManyProperties::class,
                    DivideByZeroException::class,
                    ThrowableFake::class,
                ),
                graphConcreteClasses = listOf(
                    Node::class,
                ),
            )
        }
    }
}
