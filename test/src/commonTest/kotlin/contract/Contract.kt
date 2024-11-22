package ch.softappeal.yass2.contract

import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.Interceptor
import ch.softappeal.yass2.SuspendInterceptor
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.Tunnel

enum class Gender { Female, Male }

class IntException(
    val i: Int?,
) : RuntimeException()

abstract class Id {
    abstract var id: Int
}

class PlainId(
    override var id: Int = 60,
) : Id()

class ComplexId(
    val baseId: Id = PlainId(),
    val baseIdOptional: Id? = null,
    val plainId: PlainId = PlainId(59),
    val plainIdOptional: PlainId? = null,
) : Id() {
    override var id: Int = baseId.id - 2
}

class Lists(
    val list: List<Id> = listOf(),
    val listOptional: List<Id>? = null,
    val mutableList: MutableList<Id> = mutableListOf(),
)

open class Id2 : Id() {
    override var id: Int = 60
}

class Id3 : Id2()

class IdWrapper(
    val id: Id2 = Id2(),
    val idOptional: Id2? = null,
)

class ManyProperties(
    var h: Int,
    val d: Int,
    val f: Int,
    val g: Int,
    val b: Int,
) {
    var e: Int = 0
    var j: Int = 0
    var c: Int = 0
    var a: Int = 0
    var i: Int = 0
}

class Node(val id: Int) {
    var link: Node? = null
}

class DivideByZeroException : RuntimeException()

class ThrowableFake(
    val cause: String,
    val message: String,
)

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class TestAnnotation

interface AddCalculator {
    suspend fun add(a: Int, b: Int): Int
}

@GenerateProxy
interface Calculator : AddCalculator {
    suspend fun divide(a: Int, b: Int): Int
}

expect fun Calculator.proxy(suspendIntercept: SuspendInterceptor): Calculator
expect fun ServiceId<Calculator>.proxy(tunnel: Tunnel): Calculator
expect fun ServiceId<Calculator>.service(implementation: Calculator): Service

@GenerateProxy
interface Echo {
    @TestAnnotation
    suspend fun echo(value: Any?): Any?
    suspend fun echoRequired(value: Any): Any
    suspend fun noParametersNoResult()
    suspend fun delay(milliSeconds: Int)
    suspend fun echoNode(node: Node?): Node?
    suspend fun echoNodeRequired(node: Node): Node
    suspend fun echoGeneric(map: Map<String?, Node>): Map<Int, Node>?
    suspend fun echoMonster(a: List<*>, b: List<List<String?>?>, c: Map<out Int, String>, d: Pair<*, *>): Map<in Int, String>?
}

expect fun Echo.proxy(suspendIntercept: SuspendInterceptor): Echo
expect fun ServiceId<Echo>.proxy(tunnel: Tunnel): Echo
expect fun ServiceId<Echo>.service(implementation: Echo): Service

@GenerateProxy
interface Mixed {
    fun divide(a: Int, b: Int): Int
    suspend fun suspendDivide(a: Int, b: Int): Int
    fun noParametersNoResult()
}

expect fun Mixed.proxy(intercept: Interceptor, suspendIntercept: SuspendInterceptor): Mixed
