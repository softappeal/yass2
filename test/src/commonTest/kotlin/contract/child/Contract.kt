package ch.softappeal.yass2.contract.child

import ch.softappeal.yass2.contract.child.reflect.createDumper

interface NoSuspend {
    fun x()
}

val ChildDumper = createDumper {}
