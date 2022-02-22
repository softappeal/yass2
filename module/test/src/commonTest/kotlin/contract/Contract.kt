package ch.softappeal.yass2.contract

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

class DivideByZeroException : RuntimeException()

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class TestAnnotation

interface Calculator {
    suspend fun add(a: Int, b: Int): Int
    suspend fun divide(a: Int, b: Int): Int
}

interface Echo {
    @TestAnnotation
    suspend fun echo(value: Any?): Any?

    suspend fun echoRequired(value: Any): Any

    suspend fun noParametersNoResult()

    suspend fun delay(milliSeconds: Int)
}

interface Mixed {
    fun divide(a: Int, b: Int): Int
    suspend fun suspendDivide(a: Int, b: Int): Int
    fun noParametersNoResult()
}
