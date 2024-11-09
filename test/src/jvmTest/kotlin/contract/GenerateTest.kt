package ch.softappeal.yass2.contract

import ch.softappeal.yass2.contract.child.NoSuspend
import ch.softappeal.yass2.generate.reflect.Mode
import ch.softappeal.yass2.generate.reflect.generate
import ch.softappeal.yass2.generate.reflect.generateBinarySerializerAndDumper
import ch.softappeal.yass2.generate.reflect.generateDumper
import ch.softappeal.yass2.generate.reflect.generateProxy
import kotlin.test.Test

class GenerateTest {
    @Test
    fun test() {
        generate(
            "src/commonTest/kotlin/contract/child/reflect",
            "ch.softappeal.yass2.contract.child.reflect",
            Mode.Verify,
        ) {
            generateProxy(NoSuspend::class)
            generateDumper(listOf(ManyProperties::class), listOf(Node::class))
        }
        generate(
            "src/commonTest/kotlin/contract/reflect",
            "ch.softappeal.yass2.contract.reflect",
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
