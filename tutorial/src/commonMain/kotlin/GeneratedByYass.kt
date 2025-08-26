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

package tutorial

public fun tutorial.Calculator.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): tutorial.Calculator = object : tutorial.Calculator {
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

public fun ch.softappeal.yass2.core.remote.ServiceId<tutorial.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): tutorial.Calculator =
    object : tutorial.Calculator {
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

public fun ch.softappeal.yass2.core.remote.ServiceId<tutorial.Calculator>.service(
    implementation: tutorial.Calculator,
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

public fun tutorial.NewsListener.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): tutorial.NewsListener = object : tutorial.NewsListener {
    override suspend fun notify(
        p1: kotlin.String,
    ) {
        intercept("notify", listOf(p1)) {
            this@proxy.notify(p1)
        }
    }
}

public fun ch.softappeal.yass2.core.remote.ServiceId<tutorial.NewsListener>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): tutorial.NewsListener =
    object : tutorial.NewsListener {
        override suspend fun notify(
            p1: kotlin.String,
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "notify", listOf(p1)))
                .process()
        }
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<tutorial.NewsListener>.service(
    implementation: tutorial.NewsListener,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "notify" -> implementation.notify(
                parameters[0] as kotlin.String,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public val StringEncoders: List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>> = listOf(
    // kotlin.String: 0
    // kotlin.Boolean: 1
    // kotlin.collections.List: 2
    ch.softappeal.yass2.core.serialize.string.IntStringEncoder, // 3
    tutorial.MyDateEncoder, // 4
    ch.softappeal.yass2.core.serialize.string.EnumStringEncoder(
        tutorial.Gender::class, // 5
        tutorial.Gender::valueOf,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        tutorial.Address::class, // 6
        { i ->
            writeProperty("street", i.street, 0)
            writeProperty("number", i.number, 3)
        },
        {
            tutorial.Address(
                getProperty("street") as kotlin.String,
                getProperty("number") as kotlin.Int?,
            )
        },
        "street" to -1,
        "number" to 3,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        tutorial.Person::class, // 7
        { i ->
            writeProperty("name", i.name, 0)
            writeProperty("gender", i.gender, 5)
            writeProperty("birthday", i.birthday, 4)
            writeProperty("addresses", i.addresses, 2)
        },
        {
            tutorial.Person(
                getProperty("name") as kotlin.String,
                getProperty("gender") as tutorial.Gender,
                getProperty("birthday") as tutorial.MyDate,
                getProperty("addresses") as kotlin.collections.List<tutorial.Address>,
            )
        },
        "name" to -1,
        "gender" to 5,
        "birthday" to 4,
        "addresses" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        tutorial.DivideByZeroException::class, // 8
        { i ->
        },
        {
            tutorial.DivideByZeroException(
            )
        },
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        tutorial.SubClass::class, // 9
        { i ->
            writeProperty("baseClassProperty", i.baseClassProperty, 0)
            writeProperty("subClassProperty", i.subClassProperty, 0)
        },
        {
            tutorial.SubClass(
                getProperty("baseClassProperty") as kotlin.String,
                getProperty("subClassProperty") as kotlin.String,
            )
        },
        "baseClassProperty" to -1,
        "subClassProperty" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.core.remote.Request::class, // 10
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
        ch.softappeal.yass2.core.remote.ValueReply::class, // 11
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
        ch.softappeal.yass2.core.remote.ExceptionReply::class, // 12
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
        ch.softappeal.yass2.coroutines.session.Packet::class, // 13
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
)
