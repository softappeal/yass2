package ch.softappeal.yass2.generate.ksp

class CompareKsp { // TODO: review

}

/*
private fun verify(newPackagePath: String, oldPackagePath: String, packageName: String, fileName: String) {
    val newGenerated = Path("$newPackagePath/$fileName.kt")
        .readAndFixLines()
    val oldGenerated = Path("$oldPackagePath/generated/$fileName.kt")
        .readAndFixLines()
        .replace(
            "package ch.softappeal.yass2.$packageName.generated",
            "package ch.softappeal.yass2.$packageName",
        )
    check(newGenerated == oldGenerated) {
        "'$fileName' is\n${">".repeat(120)}\n$newGenerated${"<".repeat(120)}\nbut should be\n${">".repeat(120)}\n$oldGenerated${
            "<".repeat(120)
        }"
    }
}

class KspGeneratedTest {
    @Test
    fun test() {
        fun verify(fileName: String, child: Boolean = false) {
            verify(
                "build/generated/ksp/jvm/jvmTest/kotlin/ch/softappeal/yass2/contract${if (child) "/child" else ""}",
                "src/commonTest/kotlin/contract${if (child) "/child" else ""}",
                "contract${if (child) ".child" else ""}",
                fileName,
            )
        }
        verify(GENERATED_BINARY_SERIALIZER)
        verify(GENERATED_PROXY)
        verify(GENERATED_DUMPER)
        verify(GENERATED_PROXY, true)
        verify(GENERATED_DUMPER, true)
    }

    @Test
    fun testFlow() {
        verify(
            "../yass2-coroutines/build/generated/ksp/jvm/jvmMain/kotlin/ch/softappeal/yass2/remote/coroutines",
            "src/commonTest/kotlin/remote/coroutines",
            "remote.coroutines",
            GENERATED_PROXY,
        )
    }
}

 */