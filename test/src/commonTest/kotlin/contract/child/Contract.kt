package ch.softappeal.yass2.contract.child

interface NoSuspend {
    fun x()
}

val ChildDumper = createDumper {}
