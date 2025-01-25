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

package ch.softappeal.yass2.tutorial

public fun ch.softappeal.yass2.tutorial.Calculator.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.tutorial.Calculator = object : ch.softappeal.yass2.tutorial.Calculator {
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

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.tutorial.Calculator =
    object : ch.softappeal.yass2.tutorial.Calculator {
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

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.service(
    implementation: ch.softappeal.yass2.tutorial.Calculator,
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

public fun ch.softappeal.yass2.tutorial.NewsListener.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.tutorial.NewsListener = object : ch.softappeal.yass2.tutorial.NewsListener {
    override suspend fun notify(
        p1: kotlin.String,
    ) {
        suspendIntercept("notify", listOf(p1)) {
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
            tunnel(ch.softappeal.yass2.remote.Request(id, "notify", listOf(p1)))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.NewsListener>.service(
    implementation: ch.softappeal.yass2.tutorial.NewsListener,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "notify" -> implementation.notify(
                parameters[0] as kotlin.String,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun createUtf8Encoders(): kotlin.collections.List<ch.softappeal.yass2.serialize.utf8.Utf8Encoder<*>> = listOf(
    ch.softappeal.yass2.serialize.utf8.IntUtf8Encoder,
    ch.softappeal.yass2.tutorial.MyDateEncoder,
    ch.softappeal.yass2.serialize.utf8.EnumUtf8Encoder(
        ch.softappeal.yass2.tutorial.Gender::class,
        ch.softappeal.yass2.tutorial.Gender::valueOf,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.tutorial.Address::class,
        { i ->
            writeNoId("street", 0, i.street)
            writeNoId("number", 2, i.number)
        },
        {
            val i = ch.softappeal.yass2.tutorial.Address(
                getProperty("street") as kotlin.String,
            )
            i.number = getProperty("number") as kotlin.Int?
            i
        },
        "street" to -1,
        "number" to 2,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.tutorial.Person::class,
        { i ->
            writeNoId("name", 0, i.name)
            writeNoId("gender", 4, i.gender)
            writeNoId("birthday", 3, i.birthday)
            writeNoId("addresses", 1, i.addresses)
        },
        {
            val i = ch.softappeal.yass2.tutorial.Person(
                getProperty("name") as kotlin.String,
                getProperty("gender") as ch.softappeal.yass2.tutorial.Gender,
                getProperty("birthday") as ch.softappeal.yass2.tutorial.MyDate,
                getProperty("addresses") as kotlin.collections.List<ch.softappeal.yass2.tutorial.Address>,
            )
            i
        },
        "name" to -1,
        "gender" to 4,
        "birthday" to 3,
        "addresses" to -1,
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.tutorial.DivideByZeroException::class,
        { i ->
        },
        {
            val i = ch.softappeal.yass2.tutorial.DivideByZeroException(
            )
            i
        },
    ),
    ch.softappeal.yass2.serialize.utf8.ClassUtf8Encoder(
        ch.softappeal.yass2.tutorial.SubClass::class,
        { i ->
            writeNoId("baseClassProperty", 0, i.baseClassProperty)
            writeNoId("subClassProperty", 0, i.subClassProperty)
        },
        {
            val i = ch.softappeal.yass2.tutorial.SubClass(
                getProperty("baseClassProperty") as kotlin.String,
                getProperty("subClassProperty") as kotlin.String,
            )
            i
        },
        "baseClassProperty" to -1,
        "subClassProperty" to -1,
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
