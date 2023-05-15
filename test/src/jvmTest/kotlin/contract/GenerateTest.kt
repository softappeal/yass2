package ch.softappeal.yass2.contract

import ch.softappeal.yass2.contract.child.*
import ch.softappeal.yass2.generate.*
import ch.softappeal.yass2.generate.manual.*
import ch.softappeal.yass2.remote.coroutines.*
import kotlin.test.*

private fun generatedDir(isMain: Boolean) = "build/generated/ksp/jvm/jvm${if (isMain) "Main" else "Test"}/kotlin/ch/softappeal/yass2"

class GenerateTest {
    @Test
    fun test() {
        verify("${generatedDir(false)}/contract", GENERATED_PROXY, Id::class.java.packageName) { generateProxy(listOf(Calculator::class, Echo::class, Mixed::class)) }
        verify("${generatedDir(false)}/contract/child", GENERATED_PROXY, NoSuspend::class.java.packageName) { generateProxy(listOf(NoSuspend::class)) }
        verify("${generatedDir(false)}/contract", GENERATED_BINARY_SERIALIZER, Id::class.java.packageName) { generateBinarySerializer(BaseEncoderClasses, TreeConcreteClasses, GraphConcreteClasses) }
        verify("${generatedDir(false)}/contract", GENERATED_DUMPER_PROPERTIES, Id::class.java.packageName) { generateDumperProperties(TreeConcreteClasses + GraphConcreteClasses) }
        verify("../yass2-coroutines/${generatedDir(true)}/remote/coroutines", GENERATED_PROXY, FlowService::class.java.packageName) { generateProxy(listOf(FlowService::class)) }
    }
}
