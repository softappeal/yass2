@file:GenerateDumper(
    treeConcreteClasses = [
        ManyProperties::class,
    ],
    graphConcreteClasses = [
        Node::class,
    ],
)

package ch.softappeal.yass2.contract.child

import ch.softappeal.yass2.*
import ch.softappeal.yass2.contract.*

@GenerateProxy
interface NoSuspend {
    fun x()
}
