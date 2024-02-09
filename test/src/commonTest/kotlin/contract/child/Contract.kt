@file:GenerateDumper(
    treeConcreteClasses = [
        ManyProperties::class,
    ],
    graphConcreteClasses = [
        Node::class,
    ],
)

package ch.softappeal.yass2.contract.child

import ch.softappeal.yass2.GenerateDumper
import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.contract.ManyProperties
import ch.softappeal.yass2.contract.Node

@GenerateProxy
interface NoSuspend {
    fun x()
}
