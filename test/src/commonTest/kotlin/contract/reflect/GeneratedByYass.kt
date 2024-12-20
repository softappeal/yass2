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
)

package ch.softappeal.yass2.contract.reflect

public fun ch.softappeal.yass2.contract.Calculator.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Calculator = object : ch.softappeal.yass2.contract.Calculator {
    override suspend fun add(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.contract.Calculator::add, listOf(p1, p2)) {
            this@proxy.add(p1, p2)
        } as kotlin.Int
    }

    override suspend fun divide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.contract.Calculator::divide, listOf(p1, p2)) {
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
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1, p2)))
                .process() as kotlin.Int

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1, p2)))
                .process() as kotlin.Int
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Calculator>.service(
    implementation: ch.softappeal.yass2.contract.Calculator,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.add(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            1 -> implementation.divide(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            else -> error("service with id $id has no function with id $functionId")
        }
    }

public fun ch.softappeal.yass2.contract.Echo.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Echo = object : ch.softappeal.yass2.contract.Echo {
    override suspend fun delay(
        p1: kotlin.Int,
    ) {
        suspendIntercept(ch.softappeal.yass2.contract.Echo::delay, listOf(p1)) {
            this@proxy.delay(p1)
        }
    }

    override suspend fun echo(
        p1: kotlin.Any?,
    ): kotlin.Any? {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echo, listOf(p1)) {
            this@proxy.echo(p1)
        } as kotlin.Any?
    }

    override suspend fun echoMonster(
        p1: kotlin.collections.List<*>,
        p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
        p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
        p4: kotlin.Pair<*, *>,
    ): kotlin.collections.Map<in kotlin.Int, kotlin.String>? {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echoMonster, listOf(p1, p2, p3, p4)) {
            this@proxy.echoMonster(p1, p2, p3, p4)
        } as kotlin.collections.Map<in kotlin.Int, kotlin.String>?
    }

    override suspend fun echoRequired(
        p1: kotlin.Any,
    ): kotlin.Any {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echoRequired, listOf(p1)) {
            this@proxy.echoRequired(p1)
        } as kotlin.Any
    }

    override suspend fun noParametersNoResult(
    ) {
        suspendIntercept(ch.softappeal.yass2.contract.Echo::noParametersNoResult, listOf()) {
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
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1)))
                .process()
        }

        override suspend fun echo(
            p1: kotlin.Any?,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1)))
                .process() as kotlin.Any?

        override suspend fun echoMonster(
            p1: kotlin.collections.List<*>,
            p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
            p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
            p4: kotlin.Pair<*, *>,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 2, listOf(p1, p2, p3, p4)))
                .process() as kotlin.collections.Map<in kotlin.Int, kotlin.String>?

        override suspend fun echoRequired(
            p1: kotlin.Any,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 3, listOf(p1)))
                .process() as kotlin.Any

        override suspend fun noParametersNoResult(
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 4, listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Echo>.service(
    implementation: ch.softappeal.yass2.contract.Echo,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.delay(
                parameters[0] as kotlin.Int,
            )
            1 -> implementation.echo(
                parameters[0] as kotlin.Any?,
            )
            2 -> implementation.echoMonster(
                parameters[0] as kotlin.collections.List<*>,
                parameters[1] as kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
                parameters[2] as kotlin.collections.Map<out kotlin.Int, kotlin.String>,
                parameters[3] as kotlin.Pair<*, *>,
            )
            3 -> implementation.echoRequired(
                parameters[0] as kotlin.Any,
            )
            4 -> implementation.noParametersNoResult(
            )
            else -> error("service with id $id has no function with id $functionId")
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
        return intercept(ch.softappeal.yass2.contract.Mixed::divide, listOf(p1, p2)) {
            this@proxy.divide(p1, p2)
        } as kotlin.Int
    }

    override fun noParametersNoResult(
    ) {
        intercept(ch.softappeal.yass2.contract.Mixed::noParametersNoResult, listOf()) {
            this@proxy.noParametersNoResult()
        }
    }

    override suspend fun suspendDivide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.contract.Mixed::suspendDivide, listOf(p1, p2)) {
            this@proxy.suspendDivide(p1, p2)
        } as kotlin.Int
    }
}

private class EnumEncoder1 : ch.softappeal.yass2.serialize.binary.EnumEncoder<ch.softappeal.yass2.contract.Gender>(
    ch.softappeal.yass2.contract.Gender::class, kotlin.enumValues()
)

public fun createBinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                ch.softappeal.yass2.serialize.binary.IntEncoder(),
                ch.softappeal.yass2.serialize.binary.StringEncoder(),
                ch.softappeal.yass2.serialize.binary.ByteArrayEncoder(),
                EnumEncoder1(),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IntException::class,
                    { i ->
                        writeNoIdOptional(2, i.i)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.IntException(
                            readNoIdOptional(2) as kotlin.Int?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.PlainId::class,
                    { i ->
                        writeNoIdRequired(2, i.id)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.PlainId(
                            readNoIdRequired(2) as kotlin.Int,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ComplexId::class,
                    { i ->
                        writeWithId(i.baseId)
                        writeWithId(i.baseIdOptional)
                        writeWithId(i.plainId)
                        writeWithId(i.plainIdOptional)
                        writeNoIdRequired(2, i.id)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.ComplexId(
                            readWithId() as ch.softappeal.yass2.contract.Id,
                            readWithId() as ch.softappeal.yass2.contract.Id?,
                            readWithId() as ch.softappeal.yass2.contract.PlainId,
                            readWithId() as ch.softappeal.yass2.contract.PlainId?,
                        )
                        i.id = readNoIdRequired(2) as kotlin.Int
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Lists::class,
                    { i ->
                        writeNoIdRequired(1, i.list)
                        writeNoIdOptional(1, i.listOptional)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Lists(
                            readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>,
                            readNoIdOptional(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id2::class,
                    { i ->
                        writeNoIdRequired(2, i.id)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Id2(
                        )
                        i.id = readNoIdRequired(2) as kotlin.Int
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id3::class,
                    { i ->
                        writeNoIdRequired(2, i.id)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.Id3(
                        )
                        i.id = readNoIdRequired(2) as kotlin.Int
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IdWrapper::class,
                    { i ->
                        writeWithId(i.id)
                        writeWithId(i.idOptional)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.IdWrapper(
                            readWithId() as ch.softappeal.yass2.contract.Id2,
                            readWithId() as ch.softappeal.yass2.contract.Id2?,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ManyProperties::class,
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
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.DivideByZeroException::class,
                    { _ -> },
                    {
                        val i = ch.softappeal.yass2.contract.DivideByZeroException(
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ThrowableFake::class,
                    { i ->
                        writeNoIdRequired(3, i.cause)
                        writeNoIdRequired(3, i.message)
                    },
                    {
                        val i = ch.softappeal.yass2.contract.ThrowableFake(
                            readNoIdRequired(3) as kotlin.String,
                            readNoIdRequired(3) as kotlin.String,
                        )
                        i
                    }
                ),
            )
        }
    }
