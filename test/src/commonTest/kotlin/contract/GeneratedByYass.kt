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

public fun <F, I> ch.softappeal.yass2.remote.coroutines.FlowService<F, I>.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.remote.coroutines.FlowService<F, I> = object : ch.softappeal.yass2.remote.coroutines.FlowService<F, I> {
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

public fun <F, I> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.coroutines.FlowService<F, I>>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.remote.coroutines.FlowService<F, I> =
    object : ch.softappeal.yass2.remote.coroutines.FlowService<F, I> {
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

public fun <F, I> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.coroutines.FlowService<F, I>>.service(
    implementation: ch.softappeal.yass2.remote.coroutines.FlowService<F, I>,
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

public fun createBinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder(),
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder(),
                ch.softappeal.yass2.serialize.binary.ByteArrayBinaryEncoder(),
                ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder(
                    ch.softappeal.yass2.contract.Gender::class, enumValues(),
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.IntException::class,
                    { i ->
                        writeNoIdRequired(2, i.i)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.IntException(
                            readNoIdRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.IntWrapper::class,
                    { i ->
                        writeNoIdRequired(2, i.i)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.IntWrapper(
                            readNoIdRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Optionals::class,
                    { i ->
                        writeNoIdRequired(2, i.i)
                        writeNoIdOptional(2, i.iOptional)
                        writeNoIdRequired(7, i.intWrapper)
                        writeNoIdOptional(7, i.intWrapperOptional)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Optionals(
                            readNoIdRequired(2) as kotlin.Int,
                            readNoIdOptional(2) as kotlin.Int?,
                            readNoIdRequired(7) as ch.softappeal.yass2.contract.IntWrapper,
                            readNoIdOptional(7) as ch.softappeal.yass2.contract.IntWrapper?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Lists::class,
                    { i ->
                        writeNoIdRequired(1, i.list)
                        writeNoIdOptional(1, i.listOptional)
                        writeNoIdRequired(1, i.mutableList)
                        writeNoIdOptional(1, i.mutableListOptional)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Lists(
                            readNoIdRequired(1) as kotlin.collections.List<kotlin.Int>,
                            readNoIdOptional(1) as kotlin.collections.List<kotlin.Int>?,
                            readNoIdRequired(1) as kotlin.collections.MutableList<kotlin.Int>,
                            readNoIdOptional(1) as kotlin.collections.MutableList<kotlin.Int>?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.A::class,
                    { i ->
                        writeNoIdRequired(2, i.a)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.A(
                            readNoIdRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.B::class,
                    { i ->
                        writeNoIdRequired(2, i.a)
                        writeNoIdRequired(2, i.b)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.B(
                            readNoIdRequired(2) as kotlin.Int,
                            readNoIdRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.Poly::class,
                    { i ->
                        writeWithId(i.a)
                        writeNoIdRequired(11, i.b)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Poly(
                            readWithId() as ch.softappeal.yass2.contract.A,
                            readNoIdRequired(11) as ch.softappeal.yass2.contract.B,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.ManyProperties::class,
                    { i ->
                        writeNoIdRequired(2, i.h)
                        writeNoIdRequired(2, i.d)
                        writeNoIdRequired(2, i.f)
                        writeNoIdRequired(2, i.g)
                        writeNoIdRequired(2, i.b)
                        writeNoIdRequired(2, i.a)
                        writeNoIdRequired(2, i.c)
                        writeNoIdRequired(2, i.e)
                        writeNoIdRequired(2, i.i)
                        writeNoIdRequired(2, i.j)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.ManyProperties(
                            readNoIdRequired(2) as kotlin.Int,
                            readNoIdRequired(2) as kotlin.Int,
                            readNoIdRequired(2) as kotlin.Int,
                            readNoIdRequired(2) as kotlin.Int,
                            readNoIdRequired(2) as kotlin.Int,
                        )
                        i.a = readNoIdRequired(2) as kotlin.Int
                        i.c = readNoIdRequired(2) as kotlin.Int
                        i.e = readNoIdRequired(2) as kotlin.Int
                        i.i = readNoIdRequired(2) as kotlin.Int
                        i.j = readNoIdRequired(2) as kotlin.Int
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
                        writeNoIdOptional(3, i.cause)
                        writeNoIdRequired(3, i.message)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.ThrowableFake(
                            readNoIdOptional(3) as kotlin.String?,
                            readNoIdRequired(3) as kotlin.String,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.contract.GenderWrapper::class,
                    { i ->
                        writeNoIdRequired(5, i.gender)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.GenderWrapper(
                            readNoIdRequired(5) as ch.softappeal.yass2.contract.Gender,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.Request::class,
                    { i ->
                        writeNoIdRequired(3, i.service)
                        writeNoIdRequired(3, i.function)
                        writeNoIdRequired(1, i.parameters)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.Request(
                            readNoIdRequired(3) as kotlin.String,
                            readNoIdRequired(3) as kotlin.String,
                            readNoIdRequired(1) as kotlin.collections.List<kotlin.Any?>,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ValueReply::class,
                    { i ->
                        writeWithId(i.value)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.ValueReply(
                            readWithId() as kotlin.Any?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ExceptionReply::class,
                    { i ->
                        writeWithId(i.exception)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.ExceptionReply(
                            readWithId() as kotlin.Exception,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.coroutines.Packet::class,
                    { i ->
                        writeNoIdRequired(2, i.requestNumber)
                        writeWithId(i.message)
                    },
                    {
                        val i = ch.softappeal.yass2.remote.coroutines.Packet(
                            readNoIdRequired(2) as kotlin.Int,
                            readWithId() as ch.softappeal.yass2.remote.Message,
                        )
                        i
                    }
                ),
            )
        }
    }

public fun createUtf8Encoders(): kotlin.collections.List<ch.softappeal.yass2.serialize.utf8.Utf8Encoder<*>> = listOf(
    ch.softappeal.yass2.serialize.utf8.IntUtf8Encoder(),
    ch.softappeal.yass2.serialize.utf8.ByteArrayUtf8Encoder(),
    ch.softappeal.yass2.serialize.utf8.EnumUtf8Encoder(
        ch.softappeal.yass2.contract.Gender::class,
        ch.softappeal.yass2.contract.Gender::valueOf,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.contract.IntException::class,
        { i ->
            writeNoId("i", 2, i.i)
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
            writeNoId("i", 2, i.i)
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
            writeNoId("i", 2, i.i)
            writeNoId("iOptional", 2, i.iOptional)
            writeWithId("intWrapper", i.intWrapper)
            writeWithId("intWrapperOptional", i.intWrapperOptional)
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
            writeNoId("list", 1, i.list)
            writeNoId("listOptional", 1, i.listOptional)
            writeNoId("mutableList", 1, i.mutableList)
            writeNoId("mutableListOptional", 1, i.mutableListOptional)
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
            writeNoId("a", 2, i.a)
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
            writeNoId("a", 2, i.a)
            writeNoId("b", 2, i.b)
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
            writeWithId("a", i.a)
            writeWithId("b", i.b)
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
            writeNoId("h", 2, i.h)
            writeNoId("d", 2, i.d)
            writeNoId("f", 2, i.f)
            writeNoId("g", 2, i.g)
            writeNoId("b", 2, i.b)
            writeNoId("a", 2, i.a)
            writeNoId("c", 2, i.c)
            writeNoId("e", 2, i.e)
            writeNoId("i", 2, i.i)
            writeNoId("j", 2, i.j)
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
            writeNoId("cause", 0, i.cause)
            writeNoId("message", 0, i.message)
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
            writeNoId("gender", 4, i.gender)
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
            writeNoId("service", 0, i.service)
            writeNoId("function", 0, i.function)
            writeNoId("parameters", 1, i.parameters)
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
            writeWithId("value", i.value)
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
            writeWithId("exception", i.exception)
        },
        {
            val i = ch.softappeal.yass2.remote.ExceptionReply(
                getProperty("exception") as kotlin.Exception,
            )
            i
        },
        "exception" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.remote.coroutines.Packet::class,
        { i ->
            writeNoId("requestNumber", 2, i.requestNumber)
            writeWithId("message", i.message)
        },
        {
            val i = ch.softappeal.yass2.remote.coroutines.Packet(
                getProperty("requestNumber") as kotlin.Int,
                getProperty("message") as ch.softappeal.yass2.remote.Message,
            )
            i
        },
        "requestNumber" to 2,
        "message" to -1,
    ),
)
