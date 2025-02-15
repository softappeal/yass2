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
        p1: kotlin.Exception,
    ): kotlin.Exception {
        return suspendIntercept("echoException", listOf(p1)) {
            this@proxy.echoException(p1)
        } as kotlin.Exception
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
            p1: kotlin.Exception,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "echoException", listOf(p1)))
                .process() as kotlin.Exception

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
                parameters[0] as kotlin.Exception,
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
    0: null - built-in
    1: [] - built-in
    2: kotlin.Boolean - base
    3: kotlin.Int - base
    4: kotlin.Long - base
    5: kotlin.String - base
    6: kotlin.ByteArray - base
    7: ch.softappeal.yass2.contract.Gender - enum
        0: Female
        1: Male
    8: ch.softappeal.yass2.contract.IntException - class
        i: required 3
    9: ch.softappeal.yass2.contract.IntWrapper - class
        i: required 3
    10: ch.softappeal.yass2.contract.Optionals - class
        i: required 3
        iOptional: optional 3
        intWrapper: required 9
        intWrapperOptional: optional 9
    11: ch.softappeal.yass2.contract.Lists - class
        list: required 1
        listOptional: optional 1
    12: ch.softappeal.yass2.contract.A - class
        a: required 3
    13: ch.softappeal.yass2.contract.B - class
        a: required 3
        b: required 3
    14: ch.softappeal.yass2.contract.Poly - class
        a: object
        b: required 13
    15: ch.softappeal.yass2.contract.ManyProperties - class
        h: required 3
        d: required 3
        f: required 3
        g: required 3
        b: required 3
        a: required 3
        c: required 3
        e: required 3
        i: required 3
        j: required 3
    16: ch.softappeal.yass2.contract.DivideByZeroException - class
    17: ch.softappeal.yass2.contract.ThrowableFake - class
        cause: optional 5
        message: required 5
    18: ch.softappeal.yass2.contract.Types - class
        boolean: required 2
        int: required 3
        long: required 4
        string: required 5
        bytes: required 6
        gender: required 7
    19: ch.softappeal.yass2.remote.Request - class
        service: required 5
        function: required 5
        parameters: required 1
    20: ch.softappeal.yass2.remote.ValueReply - class
        value: object
    21: ch.softappeal.yass2.remote.ExceptionReply - class
        exception: object
    22: ch.softappeal.yass2.coroutines.Packet - class
        requestNumber: required 3
        message: object
    23: ch.softappeal.yass2.contract.BodyProperty - class
        body: object
*/
public fun createBinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                ch.softappeal.yass2.serialize.binary.BooleanBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.LongBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.ByteArrayBinaryEncoder,
                ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder(
                    ch.softappeal.yass2.contract.Gender::class, enumValues(),
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.IntException::class,
                    { i ->
                        writeRequired(i.i, 3)
                    },
                    {
                        ch.softappeal.yass2.contract.IntException(
                            readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.IntWrapper::class,
                    { i ->
                        writeRequired(i.i, 3)
                    },
                    {
                        ch.softappeal.yass2.contract.IntWrapper(
                            readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Optionals::class,
                    { i ->
                        writeRequired(i.i, 3)
                        writeOptional(i.iOptional, 3)
                        writeRequired(i.intWrapper, 9)
                        writeOptional(i.intWrapperOptional, 9)
                    },
                    {
                        ch.softappeal.yass2.contract.Optionals(
                            readRequired(3) as kotlin.Int,
                            readOptional(3) as kotlin.Int?,
                            readRequired(9) as ch.softappeal.yass2.contract.IntWrapper,
                            readOptional(9) as ch.softappeal.yass2.contract.IntWrapper?,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Lists::class,
                    { i ->
                        writeRequired(i.list, 1)
                        writeOptional(i.listOptional, 1)
                    },
                    {
                        ch.softappeal.yass2.contract.Lists(
                            readRequired(1) as kotlin.collections.List<kotlin.Int>,
                            readOptional(1) as kotlin.collections.List<kotlin.Int>?,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.A::class,
                    { i ->
                        writeRequired(i.a, 3)
                    },
                    {
                        ch.softappeal.yass2.contract.A(
                            readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.B::class,
                    { i ->
                        writeRequired(i.a, 3)
                        writeRequired(i.b, 3)
                    },
                    {
                        ch.softappeal.yass2.contract.B(
                            readRequired(3) as kotlin.Int,
                            readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Poly::class,
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 13)
                    },
                    {
                        ch.softappeal.yass2.contract.Poly(
                            readObject() as ch.softappeal.yass2.contract.A,
                            readRequired(13) as ch.softappeal.yass2.contract.B,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.ManyProperties::class,
                    { i ->
                        writeRequired(i.h, 3)
                        writeRequired(i.d, 3)
                        writeRequired(i.f, 3)
                        writeRequired(i.g, 3)
                        writeRequired(i.b, 3)
                        writeRequired(i.a, 3)
                        writeRequired(i.c, 3)
                        writeRequired(i.e, 3)
                        writeRequired(i.i, 3)
                        writeRequired(i.j, 3)
                    },
                    {
                        ch.softappeal.yass2.contract.ManyProperties(
                            readRequired(3) as kotlin.Int,
                            readRequired(3) as kotlin.Int,
                            readRequired(3) as kotlin.Int,
                            readRequired(3) as kotlin.Int,
                            readRequired(3) as kotlin.Int,
                        ).apply {
                            a = readRequired(3) as kotlin.Int
                            c = readRequired(3) as kotlin.Int
                            e = readRequired(3) as kotlin.Int
                            i = readRequired(3) as kotlin.Int
                            j = readRequired(3) as kotlin.Int
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.DivideByZeroException::class,
                    { i ->
                    },
                    {
                        ch.softappeal.yass2.contract.DivideByZeroException(
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.ThrowableFake::class,
                    { i ->
                        writeOptional(i.cause, 5)
                        writeRequired(i.message, 5)
                    },
                    {
                        ch.softappeal.yass2.contract.ThrowableFake(
                            readOptional(5) as kotlin.String?,
                            readRequired(5) as kotlin.String,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Types::class,
                    { i ->
                        writeRequired(i.boolean, 2)
                        writeRequired(i.int, 3)
                        writeRequired(i.long, 4)
                        writeRequired(i.string, 5)
                        writeRequired(i.bytes, 6)
                        writeRequired(i.gender, 7)
                    },
                    {
                        ch.softappeal.yass2.contract.Types(
                            readRequired(2) as kotlin.Boolean,
                            readRequired(3) as kotlin.Int,
                            readRequired(4) as kotlin.Long,
                            readRequired(5) as kotlin.String,
                            readRequired(6) as kotlin.ByteArray,
                            readRequired(7) as ch.softappeal.yass2.contract.Gender,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.Request::class,
                    { i ->
                        writeRequired(i.service, 5)
                        writeRequired(i.function, 5)
                        writeRequired(i.parameters, 1)
                    },
                    {
                        ch.softappeal.yass2.remote.Request(
                            readRequired(5) as kotlin.String,
                            readRequired(5) as kotlin.String,
                            readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ValueReply::class,
                    { i ->
                        writeObject(i.value)
                    },
                    {
                        ch.softappeal.yass2.remote.ValueReply(
                            readObject() as kotlin.Any?,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ExceptionReply::class,
                    { i ->
                        writeObject(i.exception)
                    },
                    {
                        ch.softappeal.yass2.remote.ExceptionReply(
                            readObject() as kotlin.Exception,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.coroutines.Packet::class,
                    { i ->
                        writeRequired(i.requestNumber, 3)
                        writeObject(i.message)
                    },
                    {
                        ch.softappeal.yass2.coroutines.Packet(
                            readRequired(3) as kotlin.Int,
                            readObject() as ch.softappeal.yass2.remote.Message,
                        )
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.BodyProperty::class,
                    { i ->
                        writeObject(i.body)
                    },
                    {
                        ch.softappeal.yass2.contract.BodyProperty(
                        ).apply {
                            body = readObject() as kotlin.Any?
                        }
                    }
                ),
            )
        }
    }

/*
    0: "" - built-in
    1: [] - built-in
    2: kotlin.Boolean - base
    3: kotlin.Int - base
    4: kotlin.Long - base
    5: kotlin.ByteArray - base
    6: ch.softappeal.yass2.contract.Gender - enum
        Female
        Male
    7: ch.softappeal.yass2.contract.IntException - class
        i: 3
    8: ch.softappeal.yass2.contract.IntWrapper - class
        i: 3
    9: ch.softappeal.yass2.contract.Optionals - class
        i: 3
        iOptional: 3
        intWrapper: object
        intWrapperOptional: object
    10: ch.softappeal.yass2.contract.Lists - class
        list: 1
        listOptional: 1
    11: ch.softappeal.yass2.contract.A - class
        a: 3
    12: ch.softappeal.yass2.contract.B - class
        a: 3
        b: 3
    13: ch.softappeal.yass2.contract.Poly - class
        a: object
        b: object
    14: ch.softappeal.yass2.contract.ManyProperties - class
        h: 3
        d: 3
        f: 3
        g: 3
        b: 3
        a: 3
        c: 3
        e: 3
        i: 3
        j: 3
    15: ch.softappeal.yass2.contract.DivideByZeroException - class
    16: ch.softappeal.yass2.contract.ThrowableFake - class
        cause: 0
        message: 0
    17: ch.softappeal.yass2.contract.Types - class
        boolean: 2
        int: 3
        long: 4
        string: 0
        bytes: 5
        gender: 6
    18: ch.softappeal.yass2.remote.Request - class
        service: 0
        function: 0
        parameters: 1
    19: ch.softappeal.yass2.remote.ValueReply - class
        value: object
    20: ch.softappeal.yass2.remote.ExceptionReply - class
        exception: object
    21: ch.softappeal.yass2.coroutines.Packet - class
        requestNumber: 3
        message: object
    22: ch.softappeal.yass2.contract.BodyProperty - class
        body: object
*/
public fun createStringEncoders(): kotlin.collections.List<ch.softappeal.yass2.serialize.string.StringEncoder<*>> = listOf(
    ch.softappeal.yass2.serialize.string.BooleanStringEncoder,
    ch.softappeal.yass2.serialize.string.IntStringEncoder,
    ch.softappeal.yass2.serialize.string.LongStringEncoder,
    ch.softappeal.yass2.serialize.string.ByteArrayStringEncoder,
    ch.softappeal.yass2.serialize.string.EnumStringEncoder(
        ch.softappeal.yass2.contract.Gender::class,
        ch.softappeal.yass2.contract.Gender::valueOf,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.IntException::class,
        { i ->
            writeProperty("i", i.i, 3)
        },
        {
            ch.softappeal.yass2.contract.IntException(
                getProperty("i") as kotlin.Int,
            )
        },
        "i" to 3,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.IntWrapper::class,
        { i ->
            writeProperty("i", i.i, 3)
        },
        {
            ch.softappeal.yass2.contract.IntWrapper(
                getProperty("i") as kotlin.Int,
            )
        },
        "i" to 3,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.Optionals::class,
        { i ->
            writeProperty("i", i.i, 3)
            writeProperty("iOptional", i.iOptional, 3)
            writeProperty("intWrapper", i.intWrapper)
            writeProperty("intWrapperOptional", i.intWrapperOptional)
        },
        {
            ch.softappeal.yass2.contract.Optionals(
                getProperty("i") as kotlin.Int,
                getProperty("iOptional") as kotlin.Int?,
                getProperty("intWrapper") as ch.softappeal.yass2.contract.IntWrapper,
                getProperty("intWrapperOptional") as ch.softappeal.yass2.contract.IntWrapper?,
            )
        },
        "i" to 3,
        "iOptional" to 3,
        "intWrapper" to -1,
        "intWrapperOptional" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.Lists::class,
        { i ->
            writeProperty("list", i.list, 1)
            writeProperty("listOptional", i.listOptional, 1)
        },
        {
            ch.softappeal.yass2.contract.Lists(
                getProperty("list") as kotlin.collections.List<kotlin.Int>,
                getProperty("listOptional") as kotlin.collections.List<kotlin.Int>?,
            )
        },
        "list" to -1,
        "listOptional" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.A::class,
        { i ->
            writeProperty("a", i.a, 3)
        },
        {
            ch.softappeal.yass2.contract.A(
                getProperty("a") as kotlin.Int,
            )
        },
        "a" to 3,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.B::class,
        { i ->
            writeProperty("a", i.a, 3)
            writeProperty("b", i.b, 3)
        },
        {
            ch.softappeal.yass2.contract.B(
                getProperty("a") as kotlin.Int,
                getProperty("b") as kotlin.Int,
            )
        },
        "a" to 3,
        "b" to 3,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.Poly::class,
        { i ->
            writeProperty("a", i.a)
            writeProperty("b", i.b)
        },
        {
            ch.softappeal.yass2.contract.Poly(
                getProperty("a") as ch.softappeal.yass2.contract.A,
                getProperty("b") as ch.softappeal.yass2.contract.B,
            )
        },
        "a" to -1,
        "b" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.ManyProperties::class,
        { i ->
            writeProperty("h", i.h, 3)
            writeProperty("d", i.d, 3)
            writeProperty("f", i.f, 3)
            writeProperty("g", i.g, 3)
            writeProperty("b", i.b, 3)
            writeProperty("a", i.a, 3)
            writeProperty("c", i.c, 3)
            writeProperty("e", i.e, 3)
            writeProperty("i", i.i, 3)
            writeProperty("j", i.j, 3)
        },
        {
            ch.softappeal.yass2.contract.ManyProperties(
                getProperty("h") as kotlin.Int,
                getProperty("d") as kotlin.Int,
                getProperty("f") as kotlin.Int,
                getProperty("g") as kotlin.Int,
                getProperty("b") as kotlin.Int,
            ).apply {
                a = getProperty("a") as kotlin.Int
                c = getProperty("c") as kotlin.Int
                e = getProperty("e") as kotlin.Int
                i = getProperty("i") as kotlin.Int
                j = getProperty("j") as kotlin.Int
            }
        },
        "h" to 3,
        "d" to 3,
        "f" to 3,
        "g" to 3,
        "b" to 3,
        "a" to 3,
        "c" to 3,
        "e" to 3,
        "i" to 3,
        "j" to 3,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.DivideByZeroException::class,
        { i ->
        },
        {
            ch.softappeal.yass2.contract.DivideByZeroException(
            )
        },
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.ThrowableFake::class,
        { i ->
            writeProperty("cause", i.cause, 0)
            writeProperty("message", i.message, 0)
        },
        {
            ch.softappeal.yass2.contract.ThrowableFake(
                getProperty("cause") as kotlin.String?,
                getProperty("message") as kotlin.String,
            )
        },
        "cause" to -1,
        "message" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.Types::class,
        { i ->
            writeProperty("boolean", i.boolean, 2)
            writeProperty("int", i.int, 3)
            writeProperty("long", i.long, 4)
            writeProperty("string", i.string, 0)
            writeProperty("bytes", i.bytes, 5)
            writeProperty("gender", i.gender, 6)
        },
        {
            ch.softappeal.yass2.contract.Types(
                getProperty("boolean") as kotlin.Boolean,
                getProperty("int") as kotlin.Int,
                getProperty("long") as kotlin.Long,
                getProperty("string") as kotlin.String,
                getProperty("bytes") as kotlin.ByteArray,
                getProperty("gender") as ch.softappeal.yass2.contract.Gender,
            )
        },
        "boolean" to 2,
        "int" to 3,
        "long" to 4,
        "string" to -1,
        "bytes" to 5,
        "gender" to 6,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.remote.Request::class,
        { i ->
            writeProperty("service", i.service, 0)
            writeProperty("function", i.function, 0)
            writeProperty("parameters", i.parameters, 1)
        },
        {
            ch.softappeal.yass2.remote.Request(
                getProperty("service") as kotlin.String,
                getProperty("function") as kotlin.String,
                getProperty("parameters") as kotlin.collections.List<kotlin.Any?>,
            )
        },
        "service" to -1,
        "function" to -1,
        "parameters" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.remote.ValueReply::class,
        { i ->
            writeProperty("value", i.value)
        },
        {
            ch.softappeal.yass2.remote.ValueReply(
                getProperty("value") as kotlin.Any?,
            )
        },
        "value" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.remote.ExceptionReply::class,
        { i ->
            writeProperty("exception", i.exception)
        },
        {
            ch.softappeal.yass2.remote.ExceptionReply(
                getProperty("exception") as kotlin.Exception,
            )
        },
        "exception" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.coroutines.Packet::class,
        { i ->
            writeProperty("requestNumber", i.requestNumber, 3)
            writeProperty("message", i.message)
        },
        {
            ch.softappeal.yass2.coroutines.Packet(
                getProperty("requestNumber") as kotlin.Int,
                getProperty("message") as ch.softappeal.yass2.remote.Message,
            )
        },
        "requestNumber" to 3,
        "message" to -1,
    ),
    ch.softappeal.yass2.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.contract.BodyProperty::class,
        { i ->
            writeProperty("body", i.body)
        },
        {
            ch.softappeal.yass2.contract.BodyProperty(
            ).apply {
                body = getProperty("body") as kotlin.Any?
            }
        },
        "body" to -1,
    ),
)
