package ch.softappeal.yass2.contract

import ch.softappeal.yass2.GenerateProxy
import ch.softappeal.yass2.Interceptor
import ch.softappeal.yass2.SuspendInterceptor
import ch.softappeal.yass2.remote.Service
import ch.softappeal.yass2.remote.ServiceId
import ch.softappeal.yass2.remote.Tunnel
import ch.softappeal.yass2.serialize.binary.BinarySerializer
import ch.softappeal.yass2.serialize.binary.ByteArrayEncoder
import ch.softappeal.yass2.serialize.binary.GenerateBinarySerializer
import ch.softappeal.yass2.serialize.binary.IntEncoder
import ch.softappeal.yass2.serialize.binary.StringEncoder
import ch.softappeal.yass2.transport.binaryMessageSerializer
import ch.softappeal.yass2.transport.ktor.Transport
import ch.softappeal.yass2.transport.session.binaryPacketSerializer

enum class Gender { Female, Male }

class IntException(val i: Int) : RuntimeException()

class IntWrapper(val i: Int)

class Optionals(
    val i: Int,
    val iOptional: Int?,
    val intWrapper: IntWrapper,
    val intWrapperOptional: IntWrapper?,
)

class Lists(
    val list: List<Int>,
    val listOptional: List<Int>?,
    val mutableList: MutableList<Int>,
    val mutableListOptional: MutableList<Int>?,
)

open class A(open val a: Int)

class B(override val a: Int, val b: Int) : A(123)
// TODO: Is this really correct Kotlin code or is it a bug in the compiler?
//       It looks like "a" will be initialized twice, in the constructors of "B" and "A".
//       "B(1, 2)" seems to win over "A(123)".

class Poly(
    val a: A,
    val b: B,
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

class ThrowableFake(
    val cause: String,
    val message: String,
)

interface AddCalculator {
    suspend fun add(a: Int, b: Int): Int
}

@GenerateProxy
interface Calculator : AddCalculator {
    suspend fun divide(a: Int, b: Int): Int
}

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class TestAnnotation

@GenerateProxy
interface Echo {
    @TestAnnotation
    suspend fun echo(value: Any?): Any?
    suspend fun echoRequired(value: Any): Any
    suspend fun noParametersNoResult()
    suspend fun delay(milliSeconds: Int)
    suspend fun echoMonster(a: List<*>, b: List<List<String?>?>, c: Map<out Int, String>, d: Pair<*, *>): Map<in Int, String>?
}

@GenerateProxy
interface Mixed {
    fun divide(a: Int, b: Int): Int
    suspend fun suspendDivide(a: Int, b: Int): Int
    fun noParametersNoResult()
}

@GenerateBinarySerializer(
    baseEncoderClasses = [
        IntEncoder::class,
        StringEncoder::class,
        ByteArrayEncoder::class,
    ],
    enumClasses = [
        Gender::class,
    ],
    concreteClasses = [
        IntException::class,
        IntWrapper::class,
        Optionals::class,
        Lists::class,
        A::class,
        B::class,
        Poly::class,
        ManyProperties::class,
        DivideByZeroException::class,
        ThrowableFake::class,
    ],
)
val ContractSerializer = ch.softappeal.yass2.contract.reflect.createBinarySerializer()
val MessageSerializer = binaryMessageSerializer(ContractSerializer)
val PacketSerializer = binaryPacketSerializer(MessageSerializer)

val MessageTransport = Transport(MessageSerializer, 100, 100)
val PacketTransport = Transport(PacketSerializer, 100, 100)

val CalculatorId: ServiceId<Calculator> = ServiceId(1)
val EchoId: ServiceId<Echo> = ServiceId(2)

const val DEMO_HEADER_KEY = "Demo-Header-Key"
const val DEMO_HEADER_VALUE = "Demo-Header-Value"

// TODO: shows how to use KSP generate in none-platform code
//       https://slack-chats.kotlinlang.org/t/16366233/i-m-trying-out-kotlin-2-0-beta-3-and-it-looks-like-generated
//       Common/intermediate (= none-platform) code cannot reference generated code in the compilation of platform code.
//       Generated codes are treated as platform code (you'll have to use expect/actual).
//       see https://github.com/google/ksp/issues/2233 : Consider providing KSDeclaration.isPlatformCode() #2233

expect fun Calculator.proxy(suspendIntercept: SuspendInterceptor): Calculator
expect fun ServiceId<Calculator>.proxy(tunnel: Tunnel): Calculator
expect fun ServiceId<Calculator>.service(implementation: Calculator): Service

expect fun Echo.proxy(suspendIntercept: SuspendInterceptor): Echo
expect fun ServiceId<Echo>.proxy(tunnel: Tunnel): Echo
expect fun ServiceId<Echo>.service(implementation: Echo): Service

expect fun Mixed.proxy(intercept: Interceptor, suspendIntercept: SuspendInterceptor): Mixed

expect fun createBinarySerializer(): BinarySerializer
