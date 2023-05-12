package ch.softappeal.yass2.contract

import ch.softappeal.yass2.contract.child.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.generate.ksp.*
import ch.softappeal.yass2.generate.manual.*
import ch.softappeal.yass2.remote.coroutines.*
import kotlin.io.path.*
import kotlin.test.*

private const val MANUAL_DIR = "src/commonTest/kotlin/contract"
private const val KSP_DIR = "build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/contract"
private const val CHILD = "child"

class GenerateTest {
    @Test
    fun test() {
        fun generate(fileName: String, code: Appendable.() -> Unit) = generate(Mode.Verify, Path(MANUAL_DIR), Id::class.java.packageName, fileName, code)
        generate(GENERATED_PROXY) { generateProxy(listOf(Calculator::class, Echo::class, Mixed::class)) }
        generate(GENERATED_BINARY_SERIALIZER) { generateBinarySerializer(::BaseEncoders, TreeConcreteClasses, GraphConcreteClasses) }
        generate(GENERATED_DUMPER_PROPERTIES) { generateDumperProperties(TreeConcreteClasses + GraphConcreteClasses) }
        generate(Mode.Verify, Path("$MANUAL_DIR/$CHILD"), NoSuspend::class.java.packageName, GENERATED_PROXY) { generateProxy(listOf(NoSuspend::class)) }
    }

    @Test
    fun testFlowService() {
        generate(Mode.Verify, Path("../yass2-coroutines/src/commonMain/kotlin/remote/coroutines"), FlowService::class.java.packageName, GENERATED_PROXY) { generateProxy(listOf(FlowService::class)) }
    }

    @Test
    fun verifyKspWithManual() {
        fun verify(kspDir: String, manualDir: String, file: String) {
            Path(manualDir).resolve("$file.kt").verify(Path(kspDir).resolve(TEST_PACKAGE).resolve("$file.kt").readAndFixLines().replace(".$TEST_PACKAGE", ""))
        }
        verify(KSP_DIR, MANUAL_DIR, GENERATED_PROXY)
        verify("$KSP_DIR/$CHILD", "$MANUAL_DIR/$CHILD", GENERATED_PROXY)
    }
}
