package ch.softappeal.yass2

import ch.softappeal.yass2.reflect.*

class ReflectionDumperTest : DumperTest() {
    override val propertiesSupplier = ::reflectionDumperProperties
}
