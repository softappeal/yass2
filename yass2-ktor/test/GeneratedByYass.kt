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

package ch.softappeal.yass2.ktor

public fun ch.softappeal.yass2.ktor.Calculator.proxy(
    interceptor: ch.softappeal.yass2.remote.Interceptor,
): ch.softappeal.yass2.ktor.Calculator =
    object : ch.softappeal.yass2.ktor.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ): kotlin.Int {
            return interceptor(ch.softappeal.yass2.ktor.Calculator::add, listOf(p1, p2)) {
                this@proxy.add(p1, p2)
            } as kotlin.Int
        }

        override suspend fun delay(
            p1: kotlin.Int,
        ) {
            interceptor(ch.softappeal.yass2.ktor.Calculator::delay, listOf(p1)) {
                this@proxy.delay(p1)
            }
        }

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ): kotlin.Int {
            return interceptor(ch.softappeal.yass2.ktor.Calculator::divide, listOf(p1, p2)) {
                this@proxy.divide(p1, p2)
            } as kotlin.Int
        }

        override suspend fun noParametersNoResult(
        ) {
            interceptor(ch.softappeal.yass2.ktor.Calculator::noParametersNoResult, listOf()) {
                this@proxy.noParametersNoResult()
            }
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.ktor.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.ktor.Calculator =
    object : ch.softappeal.yass2.ktor.Calculator {
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

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.ktor.Calculator>.Service(
    implementation: ch.softappeal.yass2.ktor.Calculator,
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

public fun BinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                // kotlin.collections.List: 1
                ch.softappeal.yass2.serialize.binary.IntBinaryEncoder, // 2
                ch.softappeal.yass2.serialize.binary.StringBinaryEncoder, // 3
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.ktor.DivideByZeroException::class, // 4
                    { i ->
                    },
                    {
                        ch.softappeal.yass2.ktor.DivideByZeroException(
                        ).apply {
                        }
                    }
                ),
                ch.softappeal.yass2.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.remote.Request::class, // 5
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
                    ch.softappeal.yass2.remote.ValueReply::class, // 6
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
                    ch.softappeal.yass2.remote.ExceptionReply::class, // 7
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
                    ch.softappeal.yass2.remote.session.Packet::class, // 8
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
            )
        }
    }

public fun TextSerializer(compact: Boolean = true): ch.softappeal.yass2.serialize.text.TextSerializer =
    ch.softappeal.yass2.serialize.text.TextSerializer(
        compact,
        // kotlin.String: 0
        // kotlin.collections.List: 1
        ch.softappeal.yass2.serialize.text.IntTextEncoder, // 2
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.ktor.DivideByZeroException::class, // 3
            { i ->
            },
            {
                ch.softappeal.yass2.ktor.DivideByZeroException(
                ).apply {
                }
            },
        ),
        ch.softappeal.yass2.serialize.text.ClassTextEncoder(
            ch.softappeal.yass2.remote.Request::class, // 4
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
            ch.softappeal.yass2.remote.ValueReply::class, // 5
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
            ch.softappeal.yass2.remote.ExceptionReply::class, // 6
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
            ch.softappeal.yass2.remote.session.Packet::class, // 7
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
    )
