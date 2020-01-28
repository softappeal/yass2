package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*

private val ReflectionDumper = reflectionDumper(BaseDumper)

class DumperReflectionTest : DumperGeneratedTest() {
    override val dumper = ReflectionDumper
}
