package ch.softappeal.yass2.generate.reflect

import ch.softappeal.yass2.contract.Calculator
import ch.softappeal.yass2.contract.ContractSerializer
import ch.softappeal.yass2.contract.Echo
import ch.softappeal.yass2.contract.Mixed
import ch.softappeal.yass2.contract.child.ChildDumper
import ch.softappeal.yass2.contract.child.NoSuspend
import kotlin.io.path.Path
import kotlin.test.Test

class ReflectGeneratedTest {
    @Test
    fun generateBinarySerializer() {
        generateBinarySerializer(
            ::ContractSerializer,
            Path("src/commonTest/kotlin/contract/generated"),
            Mode.Verify,
            ".generated",
        )
    }

    @Test
    fun generateProxy() {
        generateProxy(
            setOf(Mixed::class, Calculator::class, Echo::class),
            Path("src/commonTest/kotlin/contract/generated"),
            Mode.Verify,
            ".generated",
        )
        generateProxy(
            setOf(NoSuspend::class),
            Path("src/commonTest/kotlin/contract/child/generated"),
            Mode.Verify,
            ".generated",
        )
    }

    @Test
    fun generateDumper() {
        generateDumper(
            ::ChildDumper,
            Path("src/commonTest/kotlin/contract/child/generated"),
            Mode.Verify,
            ".generated",
        )
    }
}
