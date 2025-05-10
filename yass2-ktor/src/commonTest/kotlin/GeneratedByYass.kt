@file:Suppress(
    "unused",
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "REDUNDANT_VISIBILITY_MODIFIER",
    "RedundantSuppression",
    "UNUSED_ANONYMOUS_PARAMETER",
    "KotlinRedundantDiagnosticSuppress",
)

package ch.softappeal.yass2

public fun ch.softappeal.yass2.Calculator.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.Calculator = object : ch.softappeal.yass2.Calculator {
    override suspend fun add(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return intercept("add", listOf(p1, p2)) {
            this@proxy.add(p1, p2)
        } as kotlin.Int
    }

    override suspend fun divide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return intercept("divide", listOf(p1, p2)) {
            this@proxy.divide(p1, p2)
        } as kotlin.Int
    }
}

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.Calculator =
    object : ch.softappeal.yass2.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "add", listOf(p1, p2)))
                .process() as kotlin.Int

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "divide", listOf(p1, p2)))
                .process() as kotlin.Int
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.service(
    implementation: ch.softappeal.yass2.Calculator,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
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

public fun ch.softappeal.yass2.Echo.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.Echo = object : ch.softappeal.yass2.Echo {
    override suspend fun delay(
        p1: kotlin.Int,
    ) {
        intercept("delay", listOf(p1)) {
            this@proxy.delay(p1)
        }
    }

    override suspend fun echo(
        p1: kotlin.Any?,
    ): kotlin.Any? {
        return intercept("echo", listOf(p1)) {
            this@proxy.echo(p1)
        } as kotlin.Any?
    }

    override suspend fun echoException(
        p1: kotlin.Exception,
    ): kotlin.Exception {
        return intercept("echoException", listOf(p1)) {
            this@proxy.echoException(p1)
        } as kotlin.Exception
    }

    override suspend fun echoMonster(
        p1: kotlin.collections.List<*>,
        p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
        p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
        p4: kotlin.Pair<*, *>,
    ): kotlin.collections.Map<in kotlin.Int, kotlin.String>? {
        return intercept("echoMonster", listOf(p1, p2, p3, p4)) {
            this@proxy.echoMonster(p1, p2, p3, p4)
        } as kotlin.collections.Map<in kotlin.Int, kotlin.String>?
    }

    override suspend fun echoRequired(
        p1: kotlin.Any,
    ): kotlin.Any {
        return intercept("echoRequired", listOf(p1)) {
            this@proxy.echoRequired(p1)
        } as kotlin.Any
    }

    override suspend fun noParametersNoResult(
    ) {
        intercept("noParametersNoResult", listOf()) {
            this@proxy.noParametersNoResult()
        }
    }
}

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.Echo =
    object : ch.softappeal.yass2.Echo {
        override suspend fun delay(
            p1: kotlin.Int,
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "delay", listOf(p1)))
                .process()
        }

        override suspend fun echo(
            p1: kotlin.Any?,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "echo", listOf(p1)))
                .process() as kotlin.Any?

        override suspend fun echoException(
            p1: kotlin.Exception,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "echoException", listOf(p1)))
                .process() as kotlin.Exception

        override suspend fun echoMonster(
            p1: kotlin.collections.List<*>,
            p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
            p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
            p4: kotlin.Pair<*, *>,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "echoMonster", listOf(p1, p2, p3, p4)))
                .process() as kotlin.collections.Map<in kotlin.Int, kotlin.String>?

        override suspend fun echoRequired(
            p1: kotlin.Any,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "echoRequired", listOf(p1)))
                .process() as kotlin.Any

        override suspend fun noParametersNoResult(
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "noParametersNoResult", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.service(
    implementation: ch.softappeal.yass2.Echo,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
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

public object BinarySerializer : ch.softappeal.yass2.core.serialize.binary.BinarySerializer() {
    init {
        initialize(
            ch.softappeal.yass2.core.serialize.binary.BooleanBinaryEncoder,
            ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder,
            ch.softappeal.yass2.core.serialize.binary.LongBinaryEncoder,
            ch.softappeal.yass2.core.serialize.binary.DoubleBinaryEncoder,
            ch.softappeal.yass2.core.serialize.binary.StringBinaryEncoder,
            ch.softappeal.yass2.core.serialize.binary.ByteArrayBinaryEncoder,
            ch.softappeal.yass2.core.serialize.binary.EnumBinaryEncoder(
                ch.softappeal.yass2.Gender::class, enumValues(),
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.A::class,
                { i ->
                    writeRequired(i.a, 3)
                },
                {
                    ch.softappeal.yass2.A(
                        readRequired(3) as kotlin.Int,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.B::class,
                { i ->
                    writeRequired(i.a, 3)
                    writeRequired(i.b, 3)
                },
                {
                    ch.softappeal.yass2.B(
                        readRequired(3) as kotlin.Int,
                        readRequired(3) as kotlin.Int,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.Poly::class,
                { i ->
                    writeObject(i.a)
                    writeRequired(i.b, 10)
                },
                {
                    ch.softappeal.yass2.Poly(
                        readObject() as ch.softappeal.yass2.A,
                        readRequired(10) as ch.softappeal.yass2.B,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.ManyProperties::class,
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
                    ch.softappeal.yass2.ManyProperties(
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
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.DivideByZeroException::class,
                { i ->
                },
                {
                    ch.softappeal.yass2.DivideByZeroException(
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.ThrowableFake::class,
                { i ->
                    writeOptional(i.cause, 6)
                    writeRequired(i.message, 6)
                },
                {
                    ch.softappeal.yass2.ThrowableFake(
                        readOptional(6) as kotlin.String?,
                        readRequired(6) as kotlin.String,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.Types::class,
                { i ->
                    writeRequired(i.boolean, 2)
                    writeRequired(i.int, 3)
                    writeRequired(i.long, 4)
                    writeRequired(i.double, 5)
                    writeRequired(i.string, 6)
                    writeRequired(i.bytes, 7)
                    writeRequired(i.gender, 8)
                    writeRequired(i.list, 1)
                    writeRequired(i.b, 10)
                    writeOptional(i.booleanOptional, 2)
                    writeOptional(i.intOptional, 3)
                    writeOptional(i.longOptional, 4)
                    writeOptional(i.doubleOptional, 5)
                    writeOptional(i.stringOptional, 6)
                    writeOptional(i.bytesOptional, 7)
                    writeOptional(i.genderOptional, 8)
                    writeOptional(i.listOptional, 1)
                    writeOptional(i.bOptional, 10)
                },
                {
                    ch.softappeal.yass2.Types(
                        readRequired(2) as kotlin.Boolean,
                        readRequired(3) as kotlin.Int,
                        readRequired(4) as kotlin.Long,
                        readRequired(5) as kotlin.Double,
                        readRequired(6) as kotlin.String,
                        readRequired(7) as kotlin.ByteArray,
                        readRequired(8) as ch.softappeal.yass2.Gender,
                        readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                        readRequired(10) as ch.softappeal.yass2.B,
                        readOptional(2) as kotlin.Boolean?,
                        readOptional(3) as kotlin.Int?,
                        readOptional(4) as kotlin.Long?,
                        readOptional(5) as kotlin.Double?,
                        readOptional(6) as kotlin.String?,
                        readOptional(7) as kotlin.ByteArray?,
                        readOptional(8) as ch.softappeal.yass2.Gender?,
                        readOptional(1) as kotlin.collections.List<kotlin.Any?>?,
                        readOptional(10) as ch.softappeal.yass2.B?,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.core.remote.Request::class,
                { i ->
                    writeRequired(i.service, 6)
                    writeRequired(i.function, 6)
                    writeRequired(i.parameters, 1)
                },
                {
                    ch.softappeal.yass2.core.remote.Request(
                        readRequired(6) as kotlin.String,
                        readRequired(6) as kotlin.String,
                        readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.core.remote.ValueReply::class,
                { i ->
                    writeObject(i.value)
                },
                {
                    ch.softappeal.yass2.core.remote.ValueReply(
                        readObject() as kotlin.Any?,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.core.remote.ExceptionReply::class,
                { i ->
                    writeObject(i.exception)
                },
                {
                    ch.softappeal.yass2.core.remote.ExceptionReply(
                        readObject() as kotlin.Exception,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.coroutines.session.Packet::class,
                { i ->
                    writeRequired(i.requestNumber, 3)
                    writeObject(i.message)
                },
                {
                    ch.softappeal.yass2.coroutines.session.Packet(
                        readRequired(3) as kotlin.Int,
                        readObject() as ch.softappeal.yass2.core.remote.Message,
                    )
                }
            ),
            ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                ch.softappeal.yass2.BodyProperty::class,
                { i ->
                    writeObject(i.body)
                },
                {
                    ch.softappeal.yass2.BodyProperty(
                    ).apply {
                        body = readObject() as kotlin.Any?
                    }
                }
            ),
        )
    }
}

public val StringEncoders: List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>> = listOf(
    ch.softappeal.yass2.core.serialize.string.IntStringEncoder,
    ch.softappeal.yass2.core.serialize.string.LongStringEncoder,
    @OptIn(ch.softappeal.yass2.core.NotJsPlatform::class)
    ch.softappeal.yass2.core.serialize.string.DoubleStringEncoder,
    ch.softappeal.yass2.core.serialize.string.ByteArrayStringEncoder,
    ch.softappeal.yass2.core.serialize.string.EnumStringEncoder(
        ch.softappeal.yass2.Gender::class,
        ch.softappeal.yass2.Gender::valueOf,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.A::class, false,
        { i ->
            writeProperty("a", i.a, 3)
        },
        {
            ch.softappeal.yass2.A(
                getProperty("a") as kotlin.Int,
            )
        },
        "a" to 3,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.B::class, false,
        { i ->
            writeProperty("a", i.a, 3)
            writeProperty("b", i.b, 3)
        },
        {
            ch.softappeal.yass2.B(
                getProperty("a") as kotlin.Int,
                getProperty("b") as kotlin.Int,
            )
        },
        "a" to 3,
        "b" to 3,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.Poly::class, false,
        { i ->
            writeProperty("a", i.a)
            writeProperty("b", i.b)
        },
        {
            ch.softappeal.yass2.Poly(
                getProperty("a") as ch.softappeal.yass2.A,
                getProperty("b") as ch.softappeal.yass2.B,
            )
        },
        "a" to -1,
        "b" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.ManyProperties::class, true,
        { i ->
            writeProperty("h", i.h, 3)
            writeProperty("d", i.d, 3)
            writeProperty("f", i.f, 3)
            writeProperty("g", i.g, 3)
            writeProperty("b", i.b, 3)
            startBodyProperties()
            writeProperty("a", i.a, 3)
            writeProperty("c", i.c, 3)
            writeProperty("e", i.e, 3)
            writeProperty("i", i.i, 3)
            writeProperty("j", i.j, 3)
        },
        {
            ch.softappeal.yass2.ManyProperties(
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
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.DivideByZeroException::class, false,
        { i ->
        },
        {
            ch.softappeal.yass2.DivideByZeroException(
            )
        },
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.ThrowableFake::class, false,
        { i ->
            writeProperty("cause", i.cause, 0)
            writeProperty("message", i.message, 0)
        },
        {
            ch.softappeal.yass2.ThrowableFake(
                getProperty("cause") as kotlin.String?,
                getProperty("message") as kotlin.String,
            )
        },
        "cause" to -1,
        "message" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.Types::class, false,
        { i ->
            writeProperty("boolean", i.boolean, 1)
            writeProperty("int", i.int, 3)
            writeProperty("long", i.long, 4)
            writeProperty("double", i.double, 5)
            writeProperty("string", i.string, 0)
            writeProperty("bytes", i.bytes, 6)
            writeProperty("gender", i.gender, 7)
            writeProperty("list", i.list, 2)
            writeProperty("b", i.b)
            writeProperty("booleanOptional", i.booleanOptional, 1)
            writeProperty("intOptional", i.intOptional, 3)
            writeProperty("longOptional", i.longOptional, 4)
            writeProperty("doubleOptional", i.doubleOptional, 5)
            writeProperty("stringOptional", i.stringOptional, 0)
            writeProperty("bytesOptional", i.bytesOptional, 6)
            writeProperty("genderOptional", i.genderOptional, 7)
            writeProperty("listOptional", i.listOptional, 2)
            writeProperty("bOptional", i.bOptional)
        },
        {
            ch.softappeal.yass2.Types(
                getProperty("boolean") as kotlin.Boolean,
                getProperty("int") as kotlin.Int,
                getProperty("long") as kotlin.Long,
                getProperty("double") as kotlin.Double,
                getProperty("string") as kotlin.String,
                getProperty("bytes") as kotlin.ByteArray,
                getProperty("gender") as ch.softappeal.yass2.Gender,
                getProperty("list") as kotlin.collections.List<kotlin.Any?>,
                getProperty("b") as ch.softappeal.yass2.B,
                getProperty("booleanOptional") as kotlin.Boolean?,
                getProperty("intOptional") as kotlin.Int?,
                getProperty("longOptional") as kotlin.Long?,
                getProperty("doubleOptional") as kotlin.Double?,
                getProperty("stringOptional") as kotlin.String?,
                getProperty("bytesOptional") as kotlin.ByteArray?,
                getProperty("genderOptional") as ch.softappeal.yass2.Gender?,
                getProperty("listOptional") as kotlin.collections.List<kotlin.Any?>?,
                getProperty("bOptional") as ch.softappeal.yass2.B?,
            )
        },
        "boolean" to -1,
        "int" to 3,
        "long" to 4,
        "double" to 5,
        "string" to -1,
        "bytes" to 6,
        "gender" to 7,
        "list" to -1,
        "b" to -1,
        "booleanOptional" to -1,
        "intOptional" to 3,
        "longOptional" to 4,
        "doubleOptional" to 5,
        "stringOptional" to -1,
        "bytesOptional" to 6,
        "genderOptional" to 7,
        "listOptional" to -1,
        "bOptional" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.core.remote.Request::class, false,
        { i ->
            writeProperty("service", i.service, 0)
            writeProperty("function", i.function, 0)
            writeProperty("parameters", i.parameters, 2)
        },
        {
            ch.softappeal.yass2.core.remote.Request(
                getProperty("service") as kotlin.String,
                getProperty("function") as kotlin.String,
                getProperty("parameters") as kotlin.collections.List<kotlin.Any?>,
            )
        },
        "service" to -1,
        "function" to -1,
        "parameters" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.core.remote.ValueReply::class, false,
        { i ->
            writeProperty("value", i.value)
        },
        {
            ch.softappeal.yass2.core.remote.ValueReply(
                getProperty("value") as kotlin.Any?,
            )
        },
        "value" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.core.remote.ExceptionReply::class, false,
        { i ->
            writeProperty("exception", i.exception)
        },
        {
            ch.softappeal.yass2.core.remote.ExceptionReply(
                getProperty("exception") as kotlin.Exception,
            )
        },
        "exception" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.coroutines.session.Packet::class, false,
        { i ->
            writeProperty("requestNumber", i.requestNumber, 3)
            writeProperty("message", i.message)
        },
        {
            ch.softappeal.yass2.coroutines.session.Packet(
                getProperty("requestNumber") as kotlin.Int,
                getProperty("message") as ch.softappeal.yass2.core.remote.Message,
            )
        },
        "requestNumber" to 3,
        "message" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.BodyProperty::class, true,
        { i ->
            startBodyProperties()
            writeProperty("body", i.body)
        },
        {
            ch.softappeal.yass2.BodyProperty(
            ).apply {
                body = getProperty("body") as kotlin.Any?
            }
        },
        "body" to -1,
    ),
)
