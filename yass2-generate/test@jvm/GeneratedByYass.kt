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
    "RedundantUpperBound",
    "UNNECESSARY_SAFE_CALL",
    "ForEachParameterNotUsed",
)

package ch.softappeal.yass2.generate

public fun ch.softappeal.yass2.generate.Calculator.proxy(
    interceptor: ch.softappeal.yass2.remote.Interceptor,
): ch.softappeal.yass2.generate.Calculator =
    object : ch.softappeal.yass2.generate.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ): kotlin.Int {
            return interceptor(ch.softappeal.yass2.generate.Calculator::add, listOf(p1, p2)) {
                this@proxy.add(p1, p2)
            } as kotlin.Int
        }

        override suspend fun delay(
            p1: kotlin.Int,
        ) {
            interceptor(ch.softappeal.yass2.generate.Calculator::delay, listOf(p1)) {
                this@proxy.delay(p1)
            }
        }

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ): kotlin.Int {
            return interceptor(ch.softappeal.yass2.generate.Calculator::divide, listOf(p1, p2)) {
                this@proxy.divide(p1, p2)
            } as kotlin.Int
        }

        override suspend fun noParametersNoResult(
        ) {
            interceptor(ch.softappeal.yass2.generate.Calculator::noParametersNoResult, listOf()) {
                this@proxy.noParametersNoResult()
            }
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.generate.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.generate.Calculator =
    object : ch.softappeal.yass2.generate.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "add", listOf(p1, p2)))
                .process() as kotlin.Int

        override suspend fun delay(
            p1: kotlin.Int,
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "delay", listOf(p1)))
                .process()
        }

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "divide", listOf(p1, p2)))
                .process() as kotlin.Int

        override suspend fun noParametersNoResult(
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "noParametersNoResult", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.generate.Calculator>.Service(
    implementation: ch.softappeal.yass2.generate.Calculator,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "add" -> implementation.add(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            "delay" -> implementation.delay(
                parameters[0] as kotlin.Int,
            )
            "divide" -> implementation.divide(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            "noParametersNoResult" -> implementation.noParametersNoResult(
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun <A : kotlin.Number, B : kotlin.Any?, C : kotlin.Any?> ch.softappeal.yass2.generate.GenericService<A, B, C>.proxy(
    interceptor: ch.softappeal.yass2.remote.Interceptor,
): ch.softappeal.yass2.generate.GenericService<A, B, C> =
    object : ch.softappeal.yass2.generate.GenericService<A, B, C> {
        override suspend fun echoMonster(
            p1: kotlin.collections.List<*>,
            p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
            p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
            p4: kotlin.Pair<*, *>,
        ): kotlin.collections.Map<in kotlin.Int, kotlin.String>? {
            return interceptor(ch.softappeal.yass2.generate.GenericService<A, B, C>::echoMonster, listOf(p1, p2, p3, p4)) {
                this@proxy.echoMonster(p1, p2, p3, p4)
            } as kotlin.collections.Map<in kotlin.Int, kotlin.String>?
        }

        override suspend fun service(
            p1: A,
            p2: B,
        ): C {
            return interceptor(ch.softappeal.yass2.generate.GenericService<A, B, C>::service, listOf(p1, p2)) {
                this@proxy.service(p1, p2)
            } as C
        }
    }

public fun <A : kotlin.Number, B : kotlin.Any?, C : kotlin.Any?> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.generate.GenericService<A, B, C>>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.generate.GenericService<A, B, C> =
    object : ch.softappeal.yass2.generate.GenericService<A, B, C> {
        override suspend fun echoMonster(
            p1: kotlin.collections.List<*>,
            p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
            p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
            p4: kotlin.Pair<*, *>,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "echoMonster", listOf(p1, p2, p3, p4)))
                .process() as kotlin.collections.Map<in kotlin.Int, kotlin.String>?

        override suspend fun service(
            p1: A,
            p2: B,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, "service", listOf(p1, p2)))
                .process() as C
    }

public fun <A : kotlin.Number, B : kotlin.Any?, C : kotlin.Any?> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.generate.GenericService<A, B, C>>.Service(
    implementation: ch.softappeal.yass2.generate.GenericService<A, B, C>,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "echoMonster" -> implementation.echoMonster(
                parameters[0] as kotlin.collections.List<*>,
                parameters[1] as kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
                parameters[2] as kotlin.collections.Map<out kotlin.Int, kotlin.String>,
                parameters[3] as kotlin.Pair<*, *>,
            )
            "service" -> implementation.service(
                parameters[0] as A,
                parameters[1] as B,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun BinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                // kotlin.collections.List: 1
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder, // 2
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder, // 3
                ch.softappeal.yass2.serialize.binary.EnumBinaryEncoder(
                    ch.softappeal.yass2.generate.Gender::class, kotlin.enums.enumEntries(), // 4
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.A::class, // 5
                    { i ->
                        writeRequired(i.a, 2)
                    },
                    {
                        ch.softappeal.yass2.generate.A(
                            a = readRequired(2) as kotlin.Int,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.B::class, // 6
                    { i ->
                        writeRequired(i.a, 2)
                        writeRequired(i.b, 2)
                    },
                    {
                        ch.softappeal.yass2.generate.B(
                            a = readRequired(2) as kotlin.Int,
                            b = readRequired(2) as kotlin.Int,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.Poly::class, // 7
                    { i ->
                        writeObject(i.a)
                        writeRequired(i.b, 6)
                    },
                    {
                        ch.softappeal.yass2.generate.Poly(
                            a = readObject() as ch.softappeal.yass2.generate.A,
                            b = readRequired(6) as ch.softappeal.yass2.generate.B,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.DivideByZeroException::class, // 8
                    { i ->
                    },
                    {
                        ch.softappeal.yass2.generate.DivideByZeroException(
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.ThrowableFake::class, // 9
                    { i ->
                        writeOptional(i.cause, 3)
                        writeRequired(i.message, 3)
                    },
                    {
                        ch.softappeal.yass2.generate.ThrowableFake(
                            cause = readOptional(3) as kotlin.String?,
                            message = readRequired(3) as kotlin.String,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.Request::class, // 10
                    { i ->
                        writeRequired(i.service, 3)
                        writeRequired(i.function, 3)
                        writeRequired(i.parameters, 1)
                    },
                    {
                        ch.softappeal.yass2.remote.Request(
                            service = readRequired(3) as kotlin.String,
                            function = readRequired(3) as kotlin.String,
                            parameters = readRequired(1) as kotlin.collections.List<kotlin.Any?>,
                        ).apply {
                            parameters?.forEach { it as kotlin.Any? }
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ValueReply::class, // 11
                    { i ->
                        writeObject(i.value)
                    },
                    {
                        ch.softappeal.yass2.remote.ValueReply(
                            value = readObject() as kotlin.Any?,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.ExceptionReply::class, // 12
                    { i ->
                        writeObject(i.exception)
                    },
                    {
                        ch.softappeal.yass2.remote.ExceptionReply(
                            exception = readObject() as ch.softappeal.yass2.remote.ContractException,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.session.Packet::class, // 13
                    { i ->
                        writeRequired(i.requestNumber, 2)
                        writeObject(i.message)
                    },
                    {
                        ch.softappeal.yass2.remote.session.Packet(
                            requestNumber = readRequired(2) as kotlin.Int,
                            message = readObject() as ch.softappeal.yass2.remote.Message,
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.Example::class, // 14
                    { i ->
                        writeObject(i.boolean)
                        writeRequired(i.int, 2)
                        writeOptional(i.intOptional, 2)
                        writeObject(i.any)
                        writeObject(i.anyOptional)
                        writeRequired(i.list, 1)
                        writeOptional(i.listOptional, 1)
                    },
                    {
                        ch.softappeal.yass2.generate.Example(
                            boolean = readObject() as kotlin.Boolean,
                            int = readRequired(2) as kotlin.Int,
                            intOptional = readOptional(2) as kotlin.Int?,
                            any = readObject() as kotlin.Any,
                            anyOptional = readObject() as kotlin.Any?,
                            list = readRequired(1) as kotlin.collections.List<kotlin.Int>,
                            listOptional = readOptional(1) as kotlin.collections.List<kotlin.Int>?,
                        ).apply {
                            list?.forEach { it as kotlin.Int }
                            listOptional?.forEach { it as kotlin.Int }
                        }
                    }
                ),
            )
        }
    }

public fun TextSerializer(compact: Boolean = true): ch.softappeal.yass2.serialize.text.TextSerializer =
    ch.softappeal.yass2.serialize.text.TextSerializer(
        compact,
        // kotlin.String: 0
        // kotlin.collections.List: 1
        ch.softappeal.yass2.serialize.text.IntTextEncoder, // 2
        ch.softappeal.yass2.serialize.text.EnumTextEncoder(
            ch.softappeal.yass2.generate.Gender::class, // 3
            ch.softappeal.yass2.generate.Gender::valueOf,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.generate.A::class, // 4
            { i ->
                writeProperty("a", i.a, 2)
            },
            {
                ch.softappeal.yass2.generate.A(
                    getProperty("a") as kotlin.Int,
                ).apply {
                }
            },
            "a" to 2,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.generate.B::class, // 5
            { i ->
                writeProperty("a", i.a, 2)
                writeProperty("b", i.b, 2)
            },
            {
                ch.softappeal.yass2.generate.B(
                    getProperty("a") as kotlin.Int,
                    getProperty("b") as kotlin.Int,
                ).apply {
                }
            },
            "a" to 2,
            "b" to 2,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.generate.Poly::class, // 6
            { i ->
                writeProperty("a", i.a)
                writeProperty("b", i.b)
            },
            {
                ch.softappeal.yass2.generate.Poly(
                    getProperty("a") as ch.softappeal.yass2.generate.A,
                    getProperty("b") as ch.softappeal.yass2.generate.B,
                ).apply {
                }
            },
            "a" to -1,
            "b" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.generate.DivideByZeroException::class, // 7
            { i ->
            },
            {
                ch.softappeal.yass2.generate.DivideByZeroException(
                ).apply {
                }
            },
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.generate.ThrowableFake::class, // 8
            { i ->
                writeProperty("cause", i.cause, 0)
                writeProperty("message", i.message, 0)
            },
            {
                ch.softappeal.yass2.generate.ThrowableFake(
                    getProperty("cause") as kotlin.String?,
                    getProperty("message") as kotlin.String,
                ).apply {
                }
            },
            "cause" to -1,
            "message" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.remote.Request::class, // 9
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
                ).apply {
                    parameters?.forEach { it as kotlin.Any? }
                }
            },
            "service" to -1,
            "function" to -1,
            "parameters" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.remote.ValueReply::class, // 10
            { i ->
                writeProperty("value", i.value)
            },
            {
                ch.softappeal.yass2.remote.ValueReply(
                    getProperty("value") as kotlin.Any?,
                ).apply {
                }
            },
            "value" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.remote.ExceptionReply::class, // 11
            { i ->
                writeProperty("exception", i.exception)
            },
            {
                ch.softappeal.yass2.remote.ExceptionReply(
                    getProperty("exception") as ch.softappeal.yass2.remote.ContractException,
                ).apply {
                }
            },
            "exception" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.remote.session.Packet::class, // 12
            { i ->
                writeProperty("requestNumber", i.requestNumber, 2)
                writeProperty("message", i.message)
            },
            {
                ch.softappeal.yass2.remote.session.Packet(
                    getProperty("requestNumber") as kotlin.Int,
                    getProperty("message") as ch.softappeal.yass2.remote.Message,
                ).apply {
                }
            },
            "requestNumber" to 2,
            "message" to -1,
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.generate.Example::class, // 13
            { i ->
                writeProperty("boolean", i.boolean)
                writeProperty("int", i.int, 2)
                writeProperty("intOptional", i.intOptional, 2)
                writeProperty("any", i.any)
                writeProperty("anyOptional", i.anyOptional)
                writeProperty("list", i.list, 1)
                writeProperty("listOptional", i.listOptional, 1)
            },
            {
                ch.softappeal.yass2.generate.Example(
                    getProperty("boolean") as kotlin.Boolean,
                    getProperty("int") as kotlin.Int,
                    getProperty("intOptional") as kotlin.Int?,
                    getProperty("any") as kotlin.Any,
                    getProperty("anyOptional") as kotlin.Any?,
                    getProperty("list") as kotlin.collections.List<kotlin.Int>,
                    getProperty("listOptional") as kotlin.collections.List<kotlin.Int>?,
                ).apply {
                    list?.forEach { it as kotlin.Int }
                    listOptional?.forEach { it as kotlin.Int }
                }
            },
            "boolean" to -1,
            "int" to 2,
            "intOptional" to 2,
            "any" to -1,
            "anyOptional" to -1,
            "list" to -1,
            "listOptional" to -1,
        ),
    )
