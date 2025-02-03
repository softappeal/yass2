@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "unused",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "RedundantNullableReturnType",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantSuppression",
    "UNUSED_ANONYMOUS_PARAMETER",
    "TrailingComma",
)

package ch.softappeal.yass2.contract

public fun ch.softappeal.yass2.contract.Calculator.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Calculator = object : ch.softappeal.yass2.contract.Calculator {
    override suspend fun add(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept("add", listOf(p1, p2)) {
            this@proxy.add(p1, p2)
        } as kotlin.Int
    }

    override suspend fun divide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept("divide", listOf(p1, p2)) {
            this@proxy.divide(p1, p2)
        } as kotlin.Int
    }
}

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.contract.Calculator =
    object : ch.softappeal.yass2.contract.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "add", listOf(p1, p2)))
                .process() as kotlin.Int

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "divide", listOf(p1, p2)))
                .process() as kotlin.Int
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Calculator>.service(
    implementation: ch.softappeal.yass2.contract.Calculator,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "add" -> implementation.add(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            "divide" -> implementation.divide(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun ch.softappeal.yass2.contract.Echo.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Echo = object : ch.softappeal.yass2.contract.Echo {
    override suspend fun delay(
        p1: kotlin.Int,
    ) {
        suspendIntercept("delay", listOf(p1)) {
            this@proxy.delay(p1)
        }
    }

    override suspend fun echo(
        p1: kotlin.Any?,
    ): kotlin.Any? {
        return suspendIntercept("echo", listOf(p1)) {
            this@proxy.echo(p1)
        } as kotlin.Any?
    }

    override suspend fun echoException(
        p1: kotlin.Exception /* = java.lang.Exception */,
    ): kotlin.Exception /* = java.lang.Exception */ {
        return suspendIntercept("echoException", listOf(p1)) {
            this@proxy.echoException(p1)
        } as kotlin.Exception /* = java.lang.Exception */
    }

    override suspend fun echoMonster(
        p1: kotlin.collections.List<*>,
        p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
        p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
        p4: kotlin.Pair<*, *>,
    ): kotlin.collections.Map<in kotlin.Int, kotlin.String>? {
        return suspendIntercept("echoMonster", listOf(p1, p2, p3, p4)) {
            this@proxy.echoMonster(p1, p2, p3, p4)
        } as kotlin.collections.Map<in kotlin.Int, kotlin.String>?
    }

    override suspend fun echoRequired(
        p1: kotlin.Any,
    ): kotlin.Any {
        return suspendIntercept("echoRequired", listOf(p1)) {
            this@proxy.echoRequired(p1)
        } as kotlin.Any
    }

    override suspend fun noParametersNoResult(
    ) {
        suspendIntercept("noParametersNoResult", listOf()) {
            this@proxy.noParametersNoResult()
        }
    }
}

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Echo>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.contract.Echo =
    object : ch.softappeal.yass2.contract.Echo {
        override suspend fun delay(
            p1: kotlin.Int,
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "delay", listOf(p1)))
                .process()
        }

        override suspend fun echo(
            p1: kotlin.Any?,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "echo", listOf(p1)))
                .process() as kotlin.Any?

        override suspend fun echoException(
            p1: kotlin.Exception /* = java.lang.Exception */,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "echoException", listOf(p1)))
                .process() as kotlin.Exception /* = java.lang.Exception */

        override suspend fun echoMonster(
            p1: kotlin.collections.List<*>,
            p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
            p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
            p4: kotlin.Pair<*, *>,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "echoMonster", listOf(p1, p2, p3, p4)))
                .process() as kotlin.collections.Map<in kotlin.Int, kotlin.String>?

        override suspend fun echoRequired(
            p1: kotlin.Any,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "echoRequired", listOf(p1)))
                .process() as kotlin.Any

        override suspend fun noParametersNoResult(
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "noParametersNoResult", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Echo>.service(
    implementation: ch.softappeal.yass2.contract.Echo,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "delay" -> implementation.delay(
                parameters[0] as kotlin.Int,
            )
            "echo" -> implementation.echo(
                parameters[0] as kotlin.Any?,
            )
            "echoException" -> implementation.echoException(
                parameters[0] as kotlin.Exception /* = java.lang.Exception */,
            )
            "echoMonster" -> implementation.echoMonster(
                parameters[0] as kotlin.collections.List<*>,
                parameters[1] as kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
                parameters[2] as kotlin.collections.Map<out kotlin.Int, kotlin.String>,
                parameters[3] as kotlin.Pair<*, *>,
            )
            "echoRequired" -> implementation.echoRequired(
                parameters[0] as kotlin.Any,
            )
            "noParametersNoResult" -> implementation.noParametersNoResult(
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun ch.softappeal.yass2.contract.Mixed.proxy(
    intercept: ch.softappeal.yass2.Interceptor,
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Mixed = object : ch.softappeal.yass2.contract.Mixed {
    override fun divide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return intercept("divide", listOf(p1, p2)) {
            this@proxy.divide(p1, p2)
        } as kotlin.Int
    }

    override fun noParametersNoResult(
    ) {
        intercept("noParametersNoResult", listOf()) {
            this@proxy.noParametersNoResult()
        }
    }

    override suspend fun suspendDivide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept("suspendDivide", listOf(p1, p2)) {
            this@proxy.suspendDivide(p1, p2)
        } as kotlin.Int
    }
}

public fun <F, I> ch.softappeal.yass2.coroutines.FlowService<F, I>.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.coroutines.FlowService<F, I> = object : ch.softappeal.yass2.coroutines.FlowService<F, I> {
    override suspend fun cancel(
        p1: kotlin.Int,
    ) {
        suspendIntercept("cancel", listOf(p1)) {
            this@proxy.cancel(p1)
        }
    }

    override suspend fun create(
        p1: I,
    ): kotlin.Int {
        return suspendIntercept("create", listOf(p1)) {
            this@proxy.create(p1)
        } as kotlin.Int
    }

    override suspend fun next(
        p1: kotlin.Int,
    ): F? {
        return suspendIntercept("next", listOf(p1)) {
            this@proxy.next(p1)
        } as F?
    }
}

public fun <F, I> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.coroutines.FlowService<F, I>>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.coroutines.FlowService<F, I> =
    object : ch.softappeal.yass2.coroutines.FlowService<F, I> {
        override suspend fun cancel(
            p1: kotlin.Int,
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "cancel", listOf(p1)))
                .process()
        }

        override suspend fun create(
            p1: I,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "create", listOf(p1)))
                .process() as kotlin.Int

        override suspend fun next(
            p1: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "next", listOf(p1)))
                .process() as F?
    }

public fun <F, I> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.coroutines.FlowService<F, I>>.service(
    implementation: ch.softappeal.yass2.coroutines.FlowService<F, I>,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "cancel" -> implementation.cancel(
                parameters[0] as kotlin.Int,
            )
            "create" -> implementation.create(
                parameters[0] as I,
            )
            "next" -> implementation.next(
                parameters[0] as kotlin.Int,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

/*
    1: kotlin.collections.List
    2: kotlin.Int
    3: kotlin.String
    4: kotlin.ByteArray
    5: ch.softappeal.yass2.contract.Gender
        0: Female
        1: Male
    6: ch.softappeal.yass2.contract.IntException
        i: required 2
    7: ch.softappeal.yass2.contract.IntWrapper
        i: required 2
    8: ch.softappeal.yass2.contract.Optionals
        i: required 2
        iOptional: optional 2
        intWrapper: required 7
        intWrapperOptional: optional 7
    9: ch.softappeal.yass2.contract.Lists
        list: required 1
        listOptional: optional 1
        mutableList: required 1
        mutableListOptional: optional 1
    10: ch.softappeal.yass2.contract.A
        a: required 2
    11: ch.softappeal.yass2.contract.B
        a: required 2
        b: required 2
    12: ch.softappeal.yass2.contract.Poly
        a: object
        b: required 11
    13: ch.softappeal.yass2.contract.ManyProperties
        h: required 2
        d: required 2
        f: required 2
        g: required 2
        b: required 2
        a: required 2
        c: required 2
        e: required 2
        i: required 2
        j: required 2
    14: ch.softappeal.yass2.contract.DivideByZeroException
    15: ch.softappeal.yass2.contract.ThrowableFake
        cause: optional 3
        message: required 3
    16: ch.softappeal.yass2.contract.GenderWrapper
        gender: required 5
    17: ch.softappeal.yass2.remote.Request
        service: required 3
        function: required 3
        parameters: required 1
    18: ch.softappeal.yass2.remote.ValueReply
        value: object
    19: ch.softappeal.yass2.remote.ExceptionReply
        exception: object
    20: ch.softappeal.yass2.coroutines.Packet
        requestNumber: required 2
        message: object
*/
public fun createBinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.ByteArrayBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder(
                    ch.softappeal.yass2.contract.Gender::class, enumValues(),
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.IntException::class,
                    { i ->
                        writeRequired(i.i, 2)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.IntException(
                            readRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.IntWrapper::class,
                    { i ->
                        writeRequired(i.i, 2)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.IntWrapper(
                            readRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Optionals::class,
                    { i ->
                        writeRequired(i.i, 2)
                        writeOptional(i.iOptional, 2)
                        writeRequired(i.intWrapper, 7)
                        writeOptional(i.intWrapperOptional, 7)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Optionals(
                            readRequired(2) as kotlin.Int,
                            readOptional(2) as kotlin.Int?,
                            readRequired(7) as ch.softappeal.yass2.contract.IntWrapper,
                            readOptional(7) as ch.softappeal.yass2.contract.IntWrapper?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Lists::class,
                    { i ->
                        writeRequired(i.list, 1)
                        writeOptional(i.listOptional, 1)
                        writeRequired(i.mutableList, 1)
                        writeOptional(i.mutableListOptional, 1)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Lists(
                            readRequired(1) as kotlin.collections.List<kotlin.Int>,
                            readOptional(1) as kotlin.collections.List<kotlin.Int>?,
                            readRequired(1) as kotlin.collections.MutableList<kotlin.Int>,
                            readOptional(1) as kotlin.collections.MutableList<kotlin.Int>?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.A::class,
                    { i ->
                        writeRequired(i.a, 2)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.A(
                            readRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.B::class,
                    { i ->
                        writeRequired(i.a, 2)
                        writeRequired(i.b, 2)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.B(
                            readRequired(2) as kotlin.Int,
                            readRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Poly::class,
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 11)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Poly(
                            readObject() as ch.softappeal.yass2.contract.A,
                            readRequired(11) as ch.softappeal.yass2.contract.B,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.ManyProperties::class,
                    { i ->
                        writeRequired(i.h, 2)
                        writeRequired(i.d, 2)
                        writeRequired(i.f, 2)
                        writeRequired(i.g, 2)
                        writeRequired(i.b, 2)
                        writeRequired(i.a, 2)
                        writeRequired(i.c, 2)
                        writeRequired(i.e, 2)
                        writeRequired(i.i, 2)
                        writeRequired(i.j, 2)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.ManyProperties(
                            readRequired(2) as kotlin.Int,
                            readRequired(2) as kotlin.Int,
                            readRequired(2) as kotlin.Int,
                            readRequired(2) as kotlin.Int,
                            readRequired(2) as kotlin.Int,
                        )
                        i.a = readRequired(2) as kotlin.Int
                        i.c = readRequired(2) as kotlin.Int
                        i.e = readRequired(2) as kotlin.Int
                        i.i = readRequired(2) as kotlin.Int
                        i.j = readRequired(2) as kotlin.Int
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.DivideByZeroException::class,
                    { i ->
                    },
                    {
                        val i = ch.softappeal.yass2.contract.DivideByZeroException(
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.ThrowableFake::class,
                    { i ->
                        writeOptional(i.cause, 3)
                        writeRequired(i.message, 3)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.ThrowableFake(
                            readOptional(3) as kotlin.String?,
                            readRequired(3) as kotlin.String,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.GenderWrapper::class,
                    { i ->
                        writeRequired(i.gender, 5)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.GenderWrapper(
                            readRequired(5) as ch.softappeal.yass2.contract.Gender,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.Request::class,
                    { i ->
                        writeRequired(i.service, 3)
                        writeRequired(i.function, 3)
                        writeRequired(i.parameters, 1)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.Request(
                            readRequired(3) as kotlin.String,
                            readRequired(3) as kotlin.String,
                            readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ValueReply::class,
                    { i ->
                        writeObject(i.value)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.ValueReply(
                            readObject() as kotlin.Any?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ExceptionReply::class,
                    { i ->
                        writeObject(i.exception)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.ExceptionReply(
                            readObject() as kotlin.Exception /* = java.lang.Exception */,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.coroutines.Packet::class,
                    { i ->
                        writeRequired(i.requestNumber, 2)
                        writeObject(i.message)
                    },
                    {
                        val i = ch.softappeal.yass2.coroutines.Packet(
                            readRequired(2) as kotlin.Int,
                            readObject() as ch.softappeal.yass2.remote.Message,
                        )
                        i
                    }
                ),
            )
        }
    }

public fun createUtf8Encoders(): kotlin.collections.List<ch.softappeal.yass2.serialize.utf8.Utf8Encoder<*>> = listOf(
    ch.softappeal.yass2.serialize.utf8.IntUtf8Encoder,
    ch.softappeal.yass2.serialize.utf8.ByteArrayUtf8Encoder,
    ch.softappeal.yass2.serialize.utf8.EnumUtf8Encoder(
        ch.softappeal.yass2.contract.Gender::class,
        ch.softappeal.yass2.contract.Gender::valueOf,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.IntException::class,
        { i ->
            writeProperty("i", i.i, 2)
        },
        {
            val i = ch.softappeal.yass2.contract.IntException(
                getProperty("i") as kotlin.Int,
            )
            i
        },
        "i" to 2,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.IntWrapper::class,
        { i ->
            writeProperty("i", i.i, 2)
        },
        {
            val i = ch.softappeal.yass2.contract.IntWrapper(
                getProperty("i") as kotlin.Int,
            )
            i
        },
        "i" to 2,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.Optionals::class,
        { i ->
            writeProperty("i", i.i, 2)
            writeProperty("iOptional", i.iOptional, 2)
            writeProperty("intWrapper", i.intWrapper)
            writeProperty("intWrapperOptional", i.intWrapperOptional)
        },
        {
            val i = ch.softappeal.yass2.contract.Optionals(
                getProperty("i") as kotlin.Int,
                getProperty("iOptional") as kotlin.Int?,
                getProperty("intWrapper") as ch.softappeal.yass2.contract.IntWrapper,
                getProperty("intWrapperOptional") as ch.softappeal.yass2.contract.IntWrapper?,
            )
            i
        },
        "i" to 2,
        "iOptional" to 2,
        "intWrapper" to -1,
        "intWrapperOptional" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.Lists::class,
        { i ->
            writeProperty("list", i.list, 1)
            writeProperty("listOptional", i.listOptional, 1)
            writeProperty("mutableList", i.mutableList, 1)
            writeProperty("mutableListOptional", i.mutableListOptional, 1)
        },
        {
            val i = ch.softappeal.yass2.contract.Lists(
                getProperty("list") as kotlin.collections.List<kotlin.Int>,
                getProperty("listOptional") as kotlin.collections.List<kotlin.Int>?,
                getProperty("mutableList") as kotlin.collections.MutableList<kotlin.Int>,
                getProperty("mutableListOptional") as kotlin.collections.MutableList<kotlin.Int>?,
            )
            i
        },
        "list" to -1,
        "listOptional" to -1,
        "mutableList" to -1,
        "mutableListOptional" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.A::class,
        { i ->
            writeProperty("a", i.a, 2)
        },
        {
            val i = ch.softappeal.yass2.contract.A(
                getProperty("a") as kotlin.Int,
            )
            i
        },
        "a" to 2,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.B::class,
        { i ->
            writeProperty("a", i.a, 2)
            writeProperty("b", i.b, 2)
        },
        {
            val i = ch.softappeal.yass2.contract.B(
                getProperty("a") as kotlin.Int,
                getProperty("b") as kotlin.Int,
            )
            i
        },
        "a" to 2,
        "b" to 2,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.Poly::class,
        { i ->
            writeProperty("a", i.a)
            writeProperty("b", i.b)
        },
        {
            val i = ch.softappeal.yass2.contract.Poly(
                getProperty("a") as ch.softappeal.yass2.contract.A,
                getProperty("b") as ch.softappeal.yass2.contract.B,
            )
            i
        },
        "a" to -1,
        "b" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.ManyProperties::class,
        { i ->
            writeProperty("h", i.h, 2)
            writeProperty("d", i.d, 2)
            writeProperty("f", i.f, 2)
            writeProperty("g", i.g, 2)
            writeProperty("b", i.b, 2)
            writeProperty("a", i.a, 2)
            writeProperty("c", i.c, 2)
            writeProperty("e", i.e, 2)
            writeProperty("i", i.i, 2)
            writeProperty("j", i.j, 2)
        },
        {
            val i = ch.softappeal.yass2.contract.ManyProperties(
                getProperty("h") as kotlin.Int,
                getProperty("d") as kotlin.Int,
                getProperty("f") as kotlin.Int,
                getProperty("g") as kotlin.Int,
                getProperty("b") as kotlin.Int,
            )
            i.a = getProperty("a") as kotlin.Int
            i.c = getProperty("c") as kotlin.Int
            i.e = getProperty("e") as kotlin.Int
            i.i = getProperty("i") as kotlin.Int
            i.j = getProperty("j") as kotlin.Int
            i
        },
        "h" to 2,
        "d" to 2,
        "f" to 2,
        "g" to 2,
        "b" to 2,
        "a" to 2,
        "c" to 2,
        "e" to 2,
        "i" to 2,
        "j" to 2,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.DivideByZeroException::class,
        { i ->
        },
        {
            val i = ch.softappeal.yass2.contract.DivideByZeroException(
            )
            i
        },
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.ThrowableFake::class,
        { i ->
            writeProperty("cause", i.cause, 0)
            writeProperty("message", i.message, 0)
        },
        {
            val i = ch.softappeal.yass2.contract.ThrowableFake(
                getProperty("cause") as kotlin.String?,
                getProperty("message") as kotlin.String,
            )
            i
        },
        "cause" to -1,
        "message" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.GenderWrapper::class,
        { i ->
            writeProperty("gender", i.gender, 4)
        },
        {
            val i = ch.softappeal.yass2.contract.GenderWrapper(
                getProperty("gender") as ch.softappeal.yass2.contract.Gender,
            )
            i
        },
        "gender" to 4,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.remote.Request::class,
        { i ->
            writeProperty("service", i.service, 0)
            writeProperty("function", i.function, 0)
            writeProperty("parameters", i.parameters, 1)
        },
        {
            val i = ch.softappeal.yass2.remote.Request(
                getProperty("service") as kotlin.String,
                getProperty("function") as kotlin.String,
                getProperty("parameters") as kotlin.collections.List<kotlin.Any?>,
            )
            i
        },
        "service" to -1,
        "function" to -1,
        "parameters" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.remote.ValueReply::class,
        { i ->
            writeProperty("value", i.value)
        },
        {
            val i = ch.softappeal.yass2.remote.ValueReply(
                getProperty("value") as kotlin.Any?,
            )
            i
        },
        "value" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.remote.ExceptionReply::class,
        { i ->
            writeProperty("exception", i.exception)
        },
        {
            val i = ch.softappeal.yass2.remote.ExceptionReply(
                getProperty("exception") as kotlin.Exception /* = java.lang.Exception */,
            )
            i
        },
        "exception" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.coroutines.Packet::class,
        { i ->
            writeProperty("requestNumber", i.requestNumber, 2)
            writeProperty("message", i.message)
        },
        {
            val i = ch.softappeal.yass2.coroutines.Packet(
                getProperty("requestNumber") as kotlin.Int,
                getProperty("message") as ch.softappeal.yass2.remote.Message,
            )
            i
        },
        "requestNumber" to 2,
        "message" to -1,
    ),
)
