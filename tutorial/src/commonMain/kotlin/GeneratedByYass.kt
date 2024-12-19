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

package ch.softappeal.yass2.tutorial

public fun ch.softappeal.yass2.tutorial.Calculator.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.tutorial.Calculator = object : ch.softappeal.yass2.tutorial.Calculator {
    override suspend fun add(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.tutorial.Calculator::add, listOf(p1, p2)) {
            this@proxy.add(p1, p2)
        } as kotlin.Int
    }

    override suspend fun divide(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.tutorial.Calculator::divide, listOf(p1, p2)) {
            this@proxy.divide(p1, p2)
        } as kotlin.Int
    }
}

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.tutorial.Calculator =
    object : ch.softappeal.yass2.tutorial.Calculator {
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

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.service(
    implementation: ch.softappeal.yass2.tutorial.Calculator,
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

public fun ch.softappeal.yass2.tutorial.NewsListener.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.tutorial.NewsListener = object : ch.softappeal.yass2.tutorial.NewsListener {
    override suspend fun notify(
        p1: kotlin.String,
    ) {
        suspendIntercept(ch.softappeal.yass2.tutorial.NewsListener::notify, listOf(p1)) {
            this@proxy.notify(p1)
        }
    }
}

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.NewsListener>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.tutorial.NewsListener =
    object : ch.softappeal.yass2.tutorial.NewsListener {
        override suspend fun notify(
            p1: kotlin.String,
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1)))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.NewsListener>.service(
    implementation: ch.softappeal.yass2.tutorial.NewsListener,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.notify(
                parameters[0] as kotlin.String,
            )
            else -> error("service with id $id has no function with id $functionId")
        }
    }

private class EnumEncoder1 : ch.softappeal.yass2.serialize.binary.EnumEncoder<ch.softappeal.yass2.tutorial.Gender>(
    ch.softappeal.yass2.tutorial.Gender::class, kotlin.enumValues()
)

public fun createBinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    ch.softappeal.yass2.serialize.binary.BinarySerializer(listOf(
        ch.softappeal.yass2.serialize.binary.IntEncoder(),
        ch.softappeal.yass2.serialize.binary.StringEncoder(),
        ch.softappeal.yass2.tutorial.MyDateEncoder(),
        EnumEncoder1(),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.Address::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.street)
                w.writeNoIdOptional(3, i.number)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.Address(
                    r.readNoIdRequired(4) as kotlin.String,
                )
                i.number = r.readNoIdOptional(3) as kotlin.Int?
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.Person::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.name)
                w.writeNoIdRequired(6, i.gender)
                w.writeNoIdRequired(5, i.birthday)
                w.writeNoIdRequired(1, i.addresses)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.Person(
                    r.readNoIdRequired(4) as kotlin.String,
                    r.readNoIdRequired(6) as ch.softappeal.yass2.tutorial.Gender,
                    r.readNoIdRequired(5) as ch.softappeal.yass2.tutorial.MyDate,
                    r.readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.tutorial.Address>,
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.DivideByZeroException::class, false,
            { _, _ -> },
            {
                val i = ch.softappeal.yass2.tutorial.DivideByZeroException(
                )
                i
            }
        ),
        ch.softappeal.yass2.serialize.binary.ClassEncoder(ch.softappeal.yass2.tutorial.SubClass::class, false,
            { w, i ->
                w.writeNoIdRequired(4, i.baseClassProperty)
                w.writeNoIdRequired(4, i.subClassProperty)
            },
            { r ->
                val i = ch.softappeal.yass2.tutorial.SubClass(
                    r.readNoIdRequired(4) as kotlin.String,
                    r.readNoIdRequired(4) as kotlin.String,
                )
                i
            }
        ),
    ))

public fun createDumper(dumpValue: ch.softappeal.yass2.ValueDumper): ch.softappeal.yass2.Dumper =
    ch.softappeal.yass2.createDumper(
        ch.softappeal.yass2.dumperProperties(
            ch.softappeal.yass2.tutorial.Address::class to listOf(
                ch.softappeal.yass2.tutorial.Address::number as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.tutorial.Address::street as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.tutorial.Person::class to listOf(
                ch.softappeal.yass2.tutorial.Person::addresses as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.tutorial.Person::birthday as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.tutorial.Person::gender as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.tutorial.Person::name as kotlin.reflect.KProperty1<Any, Any?>,
            ),
            ch.softappeal.yass2.tutorial.DivideByZeroException::class to listOf(
            ),
            ch.softappeal.yass2.tutorial.SubClass::class to listOf(
                ch.softappeal.yass2.tutorial.SubClass::baseClassProperty as kotlin.reflect.KProperty1<Any, Any?>,
                ch.softappeal.yass2.tutorial.SubClass::subClassProperty as kotlin.reflect.KProperty1<Any, Any?>,
            ),
        ),
        setOf(
        ),
        dumpValue,
    )
