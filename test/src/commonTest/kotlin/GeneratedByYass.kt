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

public fun ch.softappeal.yass2.Calculator.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.Calculator =
    object : ch.softappeal.yass2.Calculator {
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

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.Calculator =
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

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.service(implementation: ch.softappeal.yass2.Calculator): ch.softappeal.yass2.core.remote.Service =
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

public fun ch.softappeal.yass2.Echo.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.Echo =
    object : ch.softappeal.yass2.Echo {
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

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.Echo =
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

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.service(implementation: ch.softappeal.yass2.Echo): ch.softappeal.yass2.core.remote.Service =
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

public fun <A, B, C> ch.softappeal.yass2.GenericService<A, B, C>.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.GenericService<A, B, C> =
    object : ch.softappeal.yass2.GenericService<A, B, C> {
        override suspend fun service(
            p1: A,
            p2: B,
        ): C {
            return intercept("service", listOf(p1, p2)) {
                this@proxy.service(p1, p2)
            } as C
        }
    }

public fun <A, B, C> ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.GenericService<A, B, C>>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.GenericService<A, B, C> =
    object : ch.softappeal.yass2.GenericService<A, B, C> {
        override suspend fun service(
            p1: A,
            p2: B,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "service", listOf(p1, p2)))
                .process() as C
    }

public fun <A, B, C> ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.GenericService<A, B, C>>.service(implementation: ch.softappeal.yass2.GenericService<A, B, C>): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "service" -> implementation.service(
                parameters[0] as A,
                parameters[1] as B,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun binarySerializer(): ch.softappeal.yass2.core.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.core.serialize.binary.BinarySerializer() {
        init {
            initialize(
                // kotlin.collections.List: 1
                ch.softappeal.yass2.core.serialize.binary.BooleanBinaryEncoder, // 2
                ch.softappeal.yass2.core.serialize.binary.IntBinaryEncoder, // 3
                ch.softappeal.yass2.core.serialize.binary.StringBinaryEncoder, // 4
                ch.softappeal.yass2.core.serialize.binary.ByteArrayBinaryEncoder, // 5
                ch.softappeal.yass2.core.serialize.binary.EnumBinaryEncoder(
                    ch.softappeal.yass2.Gender::class, enumValues(), // 6
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.A::class, // 7
                    { i ->
                        writeRequired(i.a, 3)
                    },
                    {
                        ch.softappeal.yass2.A(
                            a = readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.B::class, // 8
                    { i ->
                        writeRequired(i.a, 3)
                        writeRequired(i.b, 3)
                    },
                    {
                        ch.softappeal.yass2.B(
                            a = readRequired(3) as kotlin.Int,
                            b = readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.Poly::class, // 9
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 8)
                    },
                    {
                        ch.softappeal.yass2.Poly(
                            a = readObject() as ch.softappeal.yass2.A,
                            b = readRequired(8) as ch.softappeal.yass2.B,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.ManyProperties::class, // 10
                    { i ->
                        writeRequired(i.h, 3)
                        writeRequired(i.d, 3)
                        writeRequired(i.f, 3)
                        writeRequired(i.g, 3)
                        writeRequired(i.b, 3)
                    },
                    {
                        ch.softappeal.yass2.ManyProperties(
                            h = readRequired(3) as kotlin.Int,
                            d = readRequired(3) as kotlin.Int,
                            f = readRequired(3) as kotlin.Int,
                            g = readRequired(3) as kotlin.Int,
                            b = readRequired(3) as kotlin.Int,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.DivideByZeroException::class, // 11
                    { i ->
                    },
                    {
                        ch.softappeal.yass2.DivideByZeroException(
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.ThrowableFake::class, // 12
                    { i ->
                        writeOptional(i.cause, 4)
                        writeRequired(i.message, 4)
                    },
                    {
                        ch.softappeal.yass2.ThrowableFake(
                            cause = readOptional(4) as kotlin.String?,
                            message = readRequired(4) as kotlin.String,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.Types::class, // 13
                    { i ->
                        writeRequired(i.boolean, 2)
                        writeRequired(i.int, 3)
                        writeRequired(i.string, 4)
                        writeRequired(i.bytes, 5)
                        writeRequired(i.gender, 6)
                        writeRequired(i.list, 1)
                        writeRequired(i.b, 8)
                        writeOptional(i.booleanOptional, 2)
                        writeOptional(i.intOptional, 3)
                        writeOptional(i.stringOptional, 4)
                        writeOptional(i.bytesOptional, 5)
                        writeOptional(i.genderOptional, 6)
                        writeOptional(i.listOptional, 1)
                        writeOptional(i.bOptional, 8)
                    },
                    {
                        ch.softappeal.yass2.Types(
                            boolean = readRequired(2) as kotlin.Boolean,
                            int = readRequired(3) as kotlin.Int,
                            string = readRequired(4) as kotlin.String,
                            bytes = readRequired(5) as kotlin.ByteArray,
                            gender = readRequired(6) as ch.softappeal.yass2.Gender,
                            list = readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                            b = readRequired(8) as ch.softappeal.yass2.B,
                            booleanOptional = readOptional(2) as kotlin.Boolean?,
                            intOptional = readOptional(3) as kotlin.Int?,
                            stringOptional = readOptional(4) as kotlin.String?,
                            bytesOptional = readOptional(5) as kotlin.ByteArray?,
                            genderOptional = readOptional(6) as ch.softappeal.yass2.Gender?,
                            listOptional = readOptional(1) as kotlin.collections.List<kotlin.Any?>?,
                            bOptional = readOptional(8) as ch.softappeal.yass2.B?,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.core.remote.Request::class, // 14
                    { i ->
                        writeRequired(i.service, 4)
                        writeRequired(i.function, 4)
                        writeRequired(i.parameters, 1)
                    },
                    {
                        ch.softappeal.yass2.core.remote.Request(
                            service = readRequired(4) as kotlin.String,
                            function = readRequired(4) as kotlin.String,
                            parameters = readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.core.remote.ValueReply::class, // 15
                    { i ->
                        writeObject(i.value)
                    },
                    {
                        ch.softappeal.yass2.core.remote.ValueReply(
                            value = readObject() as kotlin.Any?,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.core.remote.ExceptionReply::class, // 16
                    { i ->
                        writeObject(i.exception)
                    },
                    {
                        ch.softappeal.yass2.core.remote.ExceptionReply(
                            exception = readObject() as kotlin.Exception,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.coroutines.session.Packet::class, // 17
                    { i ->
                        writeRequired(i.requestNumber, 3)
                        writeObject(i.message)
                    },
                    {
                        ch.softappeal.yass2.coroutines.session.Packet(
                            requestNumber = readRequired(3) as kotlin.Int,
                            message = readObject() as ch.softappeal.yass2.core.remote.Message,
                        )
                    }
                ),
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.Example::class, // 18
                    { i ->
                        writeRequired(i.int, 3)
                        writeOptional(i.intOptional, 3)
                        writeObject(i.any)
                        writeObject(i.anyOptional)
                        writeRequired(i.list, 1)
                        writeOptional(i.listOptional, 1)
                    },
                    {
                        ch.softappeal.yass2.Example(
                            int = readRequired(3) as kotlin.Int,
                            intOptional = readOptional(3) as kotlin.Int?,
                            any = readObject() as kotlin.Any,
                            anyOptional = readObject() as kotlin.Any?,
                            list = readRequired(1) as kotlin.collections.List<kotlin.Int>,
                            listOptional = readOptional(1) as kotlin.collections.List<kotlin.Int>?,
                        )
                    }
                ),
            )
        }
    }

public fun stringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>> =
    listOf(
        // kotlin.String: 0
        // kotlin.Boolean: 1
        // kotlin.collections.List: 2
        ch.softappeal.yass2.core.serialize.string.IntStringEncoder, // 3
        ch.softappeal.yass2.core.serialize.string.ByteArrayStringEncoder, // 4
        ch.softappeal.yass2.core.serialize.string.EnumStringEncoder(
            ch.softappeal.yass2.Gender::class, // 5
            ch.softappeal.yass2.Gender::valueOf,
        ),
        ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.A::class, // 6
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
            ch.softappeal.yass2.B::class, // 7
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
            ch.softappeal.yass2.Poly::class, // 8
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
            ch.softappeal.yass2.ManyProperties::class, // 9
            { i ->
                writeProperty("h", i.h, 3)
                writeProperty("d", i.d, 3)
                writeProperty("f", i.f, 3)
                writeProperty("g", i.g, 3)
                writeProperty("b", i.b, 3)
            },
            {
                ch.softappeal.yass2.ManyProperties(
                    getProperty("h") as kotlin.Int,
                    getProperty("d") as kotlin.Int,
                    getProperty("f") as kotlin.Int,
                    getProperty("g") as kotlin.Int,
                    getProperty("b") as kotlin.Int,
                )
            },
            "h" to 3,
            "d" to 3,
            "f" to 3,
            "g" to 3,
            "b" to 3,
        ),
        ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.DivideByZeroException::class, // 10
            { i ->
            },
            {
                ch.softappeal.yass2.DivideByZeroException(
                )
            },
        ),
        ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.ThrowableFake::class, // 11
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
            ch.softappeal.yass2.Types::class, // 12
            { i ->
                writeProperty("boolean", i.boolean, 1)
                writeProperty("int", i.int, 3)
                writeProperty("string", i.string, 0)
                writeProperty("bytes", i.bytes, 4)
                writeProperty("gender", i.gender, 5)
                writeProperty("list", i.list, 2)
                writeProperty("b", i.b)
                writeProperty("booleanOptional", i.booleanOptional, 1)
                writeProperty("intOptional", i.intOptional, 3)
                writeProperty("stringOptional", i.stringOptional, 0)
                writeProperty("bytesOptional", i.bytesOptional, 4)
                writeProperty("genderOptional", i.genderOptional, 5)
                writeProperty("listOptional", i.listOptional, 2)
                writeProperty("bOptional", i.bOptional)
            },
            {
                ch.softappeal.yass2.Types(
                    getProperty("boolean") as kotlin.Boolean,
                    getProperty("int") as kotlin.Int,
                    getProperty("string") as kotlin.String,
                    getProperty("bytes") as kotlin.ByteArray,
                    getProperty("gender") as ch.softappeal.yass2.Gender,
                    getProperty("list") as kotlin.collections.List<kotlin.Any?>,
                    getProperty("b") as ch.softappeal.yass2.B,
                    getProperty("booleanOptional") as kotlin.Boolean?,
                    getProperty("intOptional") as kotlin.Int?,
                    getProperty("stringOptional") as kotlin.String?,
                    getProperty("bytesOptional") as kotlin.ByteArray?,
                    getProperty("genderOptional") as ch.softappeal.yass2.Gender?,
                    getProperty("listOptional") as kotlin.collections.List<kotlin.Any?>?,
                    getProperty("bOptional") as ch.softappeal.yass2.B?,
                )
            },
            "boolean" to -1,
            "int" to 3,
            "string" to -1,
            "bytes" to 4,
            "gender" to 5,
            "list" to -1,
            "b" to -1,
            "booleanOptional" to -1,
            "intOptional" to 3,
            "stringOptional" to -1,
            "bytesOptional" to 4,
            "genderOptional" to 5,
            "listOptional" to -1,
            "bOptional" to -1,
        ),
        ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
            ch.softappeal.yass2.core.remote.Request::class, // 13
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
            ch.softappeal.yass2.core.remote.ValueReply::class, // 14
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
            ch.softappeal.yass2.core.remote.ExceptionReply::class, // 15
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
            ch.softappeal.yass2.coroutines.session.Packet::class, // 16
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
            ch.softappeal.yass2.Example::class, // 17
            { i ->
                writeProperty("int", i.int, 3)
                writeProperty("intOptional", i.intOptional, 3)
                writeProperty("any", i.any)
                writeProperty("anyOptional", i.anyOptional)
                writeProperty("list", i.list, 2)
                writeProperty("listOptional", i.listOptional, 2)
            },
            {
                ch.softappeal.yass2.Example(
                    getProperty("int") as kotlin.Int,
                    getProperty("intOptional") as kotlin.Int?,
                    getProperty("any") as kotlin.Any,
                    getProperty("anyOptional") as kotlin.Any?,
                    getProperty("list") as kotlin.collections.List<kotlin.Int>,
                    getProperty("listOptional") as kotlin.collections.List<kotlin.Int>?,
                )
            },
            "int" to 3,
            "intOptional" to 3,
            "any" to -1,
            "anyOptional" to -1,
            "list" to -1,
            "listOptional" to -1,
        ),
    )
