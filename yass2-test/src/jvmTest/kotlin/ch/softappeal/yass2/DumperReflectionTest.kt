package ch.softappeal.yass2

import ch.softappeal.yass2.contract.*
import ch.softappeal.yass2.reflect.*

private val ReflectionDumper = reflectionDumper(BaseDumper)

class DumperReflectionTest : DumperGeneratedTest() {
    override val dumper = ReflectionDumper
}
