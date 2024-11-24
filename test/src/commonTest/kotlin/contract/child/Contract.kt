package ch.softappeal.yass2.contract.child

import ch.softappeal.yass2.Dumper
import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.Interceptor
import ch.softappeal.yass2.ValueDumper
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Node
import ch.softappeal.yass2.contract.child.reflect.createDumper

@GenerateProxy
interface NoSuspend {
    fun x()
}

expect fun NoSuspend.proxy(intercept: Interceptor): NoSuspend

expect fun createDumper(dumpValue: ValueDumper): Dumper

@GenerateDumper(
    treeConcreteClasses = [
        ManyProperties::class,
    ],
    graphConcreteClasses = [
        Node::class,
    ],
)
val ChildDumper = createDumper {}
