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

public fun createBinarySerializer(): ch.softappeal.yass2.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.serialize.binary.BinarySerializer() {
        init {
            initialize(
                ch.softappeal.yass2.serialize.binary.IntEncoder(),
                ch.softappeal.yass2.serialize.binary.StringEncoder(),
                ch.softappeal.yass2.tutorial.MyDateEncoder(),
                ch.softappeal.yass2.serialize.binary.EnumEncoder(ch.softappeal.yass2.tutorial.Gender::class, enumValues()),
                ch.softappeal.yass2.serialize.binary.Encoder(ch.softappeal.yass2.tutorial.Address::class,
                    { i ->
                        writeNoIdRequired(3, i.street)
                        writeNoIdOptional(2, i.number)
                    },
                    {
                        val i = ch.softappeal.yass2.tutorial.Address(
                            readNoIdRequired(3) as kotlin.String,
                        )
                        i.number = readNoIdOptional(2) as kotlin.Int?
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.Encoder(ch.softappeal.yass2.tutorial.Person::class,
                    { i ->
                        writeNoIdRequired(3, i.name)
                        writeNoIdRequired(5, i.gender)
                        writeNoIdRequired(4, i.birthday)
                        writeNoIdRequired(1, i.addresses)
                    },
                    {
                        val i = ch.softappeal.yass2.tutorial.Person(
                            readNoIdRequired(3) as kotlin.String,
                            readNoIdRequired(5) as ch.softappeal.yass2.tutorial.Gender,
                            readNoIdRequired(4) as ch.softappeal.yass2.tutorial.MyDate,
                            readNoIdRequired(1) as kotlin.collections.List<ch.softappeal.yass2.tutorial.Address>,
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.Encoder(ch.softappeal.yass2.tutorial.DivideByZeroException::class,
                    { _ -> },
                    {
                        val i = ch.softappeal.yass2.tutorial.DivideByZeroException(
                        )
                        i
                    }
                ),
                ch.softappeal.yass2.serialize.binary.Encoder(ch.softappeal.yass2.tutorial.SubClass::class,
                    { i ->
                        writeNoIdRequired(3, i.baseClassProperty)
                        writeNoIdRequired(3, i.subClassProperty)
                    },
                    {
                        val i = ch.softappeal.yass2.tutorial.SubClass(
                            readNoIdRequired(3) as kotlin.String,
                            readNoIdRequired(3) as kotlin.String,
                        )
                        i
                    }
                ),
            )
        }
    }
