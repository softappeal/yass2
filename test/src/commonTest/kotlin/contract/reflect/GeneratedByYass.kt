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

    override suspend fun echoGeneric(
        p1: kotlin.collections.Map<kotlin.String?, ch.softappeal.yass2.contract.Node>,
    ): kotlin.collections.Map<kotlin.Int, ch.softappeal.yass2.contract.Node>? {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echoGeneric, listOf(p1)) {
            this@proxy.echoGeneric(p1)
        } as kotlin.collections.Map<kotlin.Int, ch.softappeal.yass2.contract.Node>?
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

    override suspend fun echoNode(
        p1: ch.softappeal.yass2.contract.Node?,
    ): ch.softappeal.yass2.contract.Node? {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echoNode, listOf(p1)) {
            this@proxy.echoNode(p1)
        } as ch.softappeal.yass2.contract.Node?
    }

    override suspend fun echoNodeRequired(
        p1: ch.softappeal.yass2.contract.Node,
    ): ch.softappeal.yass2.contract.Node {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echoNodeRequired, listOf(p1)) {
            this@proxy.echoNodeRequired(p1)
        } as ch.softappeal.yass2.contract.Node
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

        override suspend fun echoGeneric(
            p1: kotlin.collections.Map<kotlin.String?, ch.softappeal.yass2.contract.Node>,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 2, listOf(p1)))
                .process() as kotlin.collections.Map<kotlin.Int, ch.softappeal.yass2.contract.Node>?

        override suspend fun echoMonster(
            p1: kotlin.collections.List<*>,
            p2: kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
            p3: kotlin.collections.Map<out kotlin.Int, kotlin.String>,
            p4: kotlin.Pair<*, *>,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 3, listOf(p1, p2, p3, p4)))
                .process() as kotlin.collections.Map<in kotlin.Int, kotlin.String>?

        override suspend fun echoNode(
            p1: ch.softappeal.yass2.contract.Node?,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 4, listOf(p1)))
                .process() as ch.softappeal.yass2.contract.Node?

        override suspend fun echoNodeRequired(
            p1: ch.softappeal.yass2.contract.Node,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 5, listOf(p1)))
                .process() as ch.softappeal.yass2.contract.Node

        override suspend fun echoRequired(
            p1: kotlin.Any,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 6, listOf(p1)))
                .process() as kotlin.Any

        override suspend fun noParametersNoResult(
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 7, listOf()))
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
            2 -> implementation.echoGeneric(
                parameters[0] as kotlin.collections.Map<kotlin.String?, ch.softappeal.yass2.contract.Node>,
            )
            3 -> implementation.echoMonster(
                parameters[0] as kotlin.collections.List<*>,
                parameters[1] as kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>,
                parameters[2] as kotlin.collections.Map<out kotlin.Int, kotlin.String>,
                parameters[3] as kotlin.Pair<*, *>,
            )
            4 -> implementation.echoNode(
                parameters[0] as ch.softappeal.yass2.contract.Node?,
            )
            5 -> implementation.echoNodeRequired(
                parameters[0] as ch.softappeal.yass2.contract.Node,
            )
            6 -> implementation.echoRequired(
                parameters[0] as kotlin.Any,
            )
            7 -> implementation.noParametersNoResult(
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

public fun createSerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(listOf(
        ch.softappeal.yass2.serialize.binary.IntEncoder(),
        ch.softappeal.yass2.serialize.binary.StringEncoder(),
        ch.softappeal.yass2.serialize.binary.ByteArrayEncoder(),
        EnumEncoder1(),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IntException::class, false,
            { w, i ->
                w.writeNoIdOptional(3, i.i)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.IntException(
                    r.readNoIdOptional(3) as kotlin.Int?,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.PlainId::class, false,
            { w, i ->
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.PlainId(
                    r.readNoIdRequired(3) as kotlin.Int,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ComplexId::class, false,
            { w, i ->
                w.writeWithId(i.baseId)
                w.writeWithId(i.baseIdOptional)
                w.writeWithId(i.plainId)
                w.writeWithId(i.plainIdOptional)
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.ComplexId(
                    r.readWithId() as ch.softappeal.yass2.contract.Id,
                    r.readWithId() as ch.softappeal.yass2.contract.Id?,
                    r.readWithId() as ch.softappeal.yass2.contract.PlainId,
                    r.readWithId() as ch.softappeal.yass2.contract.PlainId?,
                )
                i.id = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Lists::class, false,
            { w, i ->
                w.writeNoIdRequired(1, i.list)
                w.writeNoIdOptional(1, i.listOptional)
                w.writeNoIdRequired(1, i.mutableList)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.Lists(
                    r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>,
                    r.readNoIdOptional(1) as kotlin.collections.List<ch.softappeal.yass2.contract.Id>?,
                    r.readNoIdRequired(1) as kotlin.collections.MutableList<ch.softappeal.yass2.contract.Id>,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id2::class, false,
            { w, i ->
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.Id2(
                )
                i.id = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Id3::class, false,
            { w, i ->
                w.writeNoIdRequired(3, i.id)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.Id3(
                )
                i.id = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.IdWrapper::class, false,
            { w, i ->
                w.writeWithId(i.id)
                w.writeWithId(i.idOptional)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.IdWrapper(
                    r.readWithId() as ch.softappeal.yass2.contract.Id2,
                    r.readWithId() as ch.softappeal.yass2.contract.Id2?,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ManyProperties::class, false,
            { w, i ->
                w.writeNoIdRequired(3, i.h)
                w.writeNoIdRequired(3, i.d)
                w.writeNoIdRequired(3, i.f)
                w.writeNoIdRequired(3, i.g)
                w.writeNoIdRequired(3, i.b)
                w.writeNoIdRequired(3, i.a)
                w.writeNoIdRequired(3, i.c)
                w.writeNoIdRequired(3, i.e)
                w.writeNoIdRequired(3, i.i)
                w.writeNoIdRequired(3, i.j)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.ManyProperties(
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                    r.readNoIdRequired(3) as kotlin.Int,
                )
                i.a = r.readNoIdRequired(3) as kotlin.Int
                i.c = r.readNoIdRequired(3) as kotlin.Int
                i.e = r.readNoIdRequired(3) as kotlin.Int
                i.i = r.readNoIdRequired(3) as kotlin.Int
                i.j = r.readNoIdRequired(3) as kotlin.Int
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.DivideByZeroException::class, false,
            { _, _ -> },
            {
                val i = ch.softappeal.yass2.contract.DivideByZeroException(
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.ThrowableFake::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.cause)
                w.writeNoIdRequired(4, i.message)
            },
            { r ->
                val i = ch.softappeal.yass2.contract.ThrowableFake(
                    r.readNoIdRequired(4) as kotlin.String,
                    r.readNoIdRequired(4) as kotlin.String,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.contract.Node::class, true,
            { w, i ->
                w.writeNoIdRequired(3, i.id)
                w.writeWithId(i.link)
            },
            { r ->
                val i = r.created(ch.softappeal.yass2.contract.Node(
                    r.readNoIdRequired(3) as kotlin.Int,
                ))
                i.link = r.readWithId() as ch.softappeal.yass2.contract.Node?
                i
            }
        ),
    ))

public fun createDumper(dumpValue: ch.softappeal.yass2.ValueDumper): ch.softappeal.yass2.Dumper =
    ch.softappeal.yass2.createDumper(
        ch.softappeal.yass2.dumperProperties(
            ch.softappeal.yass2.contract.IntException::class to listOf(
                ch.softappeal.yass2.contract.IntException::i as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.PlainId::class to listOf(
                ch.softappeal.yass2.contract.PlainId::id as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.ComplexId::class to listOf(
                ch.softappeal.yass2.contract.ComplexId::baseId as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::baseIdOptional as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::id as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::plainId as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ComplexId::plainIdOptional as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Lists::class to listOf(
                ch.softappeal.yass2.contract.Lists::list as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.Lists::listOptional as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.Lists::mutableList as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Id2::class to listOf(
                ch.softappeal.yass2.contract.Id2::id as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Id3::class to listOf(
                ch.softappeal.yass2.contract.Id3::id as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.IdWrapper::class to listOf(
                ch.softappeal.yass2.contract.IdWrapper::id as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.IdWrapper::idOptional as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.ManyProperties::class to listOf(
                ch.softappeal.yass2.contract.ManyProperties::a as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::b as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::c as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::d as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::e as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::f as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::g as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::h as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::i as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ManyProperties::j as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.DivideByZeroException::class to listOf(
            ),
            ch.softappeal.yass2.contract.ThrowableFake::class to listOf(
                ch.softappeal.yass2.contract.ThrowableFake::cause as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.ThrowableFake::message as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.contract.Node::class to listOf(
                ch.softappeal.yass2.contract.Node::id as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.contract.Node::link as kotlin.reflect.KProperty1<Any, Any?>,
            ),
        ),
        setOf(
            ch.softappeal.yass2.contract.Node::class,
        ),
        dumpValue,
    )
