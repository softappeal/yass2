package ch.softappeal.yass2.core.serialize.string

import kotlin.test.Test
import kotlin.test.assertFails

class StringEncodersTestPlatform {
    @Test
    fun test() {
        assertFails { doubleNotJsPlatform() }
    }
}
