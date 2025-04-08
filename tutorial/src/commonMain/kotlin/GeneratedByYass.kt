@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "REDUNDANT_VISIBILITY_MODIFIER",
    "RedundantSuppression",
    "UNUSED_ANONYMOUS_PARAMETER",
)

package ch.softappeal.yass2.tutorial

public fun ch.softappeal.yass2.tutorial.Calculator.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.tutorial.Calculator = object : ch.softappeal.yass2.tutorial.Calculator {
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

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.tutorial.Calculator =
    object : ch.softappeal.yass2.tutorial.Calculator {
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

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.service(
    implementation: ch.softappeal.yass2.tutorial.Calculator,
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

public fun ch.softappeal.yass2.tutorial.NewsListener.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.tutorial.NewsListener = object : ch.softappeal.yass2.tutorial.NewsListener {
    override suspend fun notify(
        p1: kotlin.String,
    ) {
        intercept("notify", listOf(p1)) {
            this@proxy.notify(p1)
        }
    }
}

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.tutorial.NewsListener>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.tutorial.NewsListener =
    object : ch.softappeal.yass2.tutorial.NewsListener {
        override suspend fun notify(
            p1: kotlin.String,
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "notify", listOf(p1)))
                .process()
        }
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.tutorial.NewsListener>.service(
    implementation: ch.softappeal.yass2.tutorial.NewsListener,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "notify" -> implementation.notify(
                parameters[0] as kotlin.String,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

/*
    0: "" - built-in
    1: true/false - built-in
    2: [] - built-in
    3: kotlin.Int - base
    4: ch.softappeal.yass2.tutorial.MyDate - base
    5: ch.softappeal.yass2.tutorial.Gender - enum
        Female
        Male
    6: ch.softappeal.yass2.tutorial.Address - class
        street: 0
        number: 3
    7: ch.softappeal.yass2.tutorial.Person - class
        name: 0
        gender: 5
        birthday: 4
        addresses: 2
    8: ch.softappeal.yass2.tutorial.DivideByZeroException - class
    9: ch.softappeal.yass2.tutorial.SubClass - class
        baseClassProperty: 0
        subClassProperty: 0
    10: ch.softappeal.yass2.core.remote.Request - class
        service: 0
        function: 0
        parameters: 2
    11: ch.softappeal.yass2.core.remote.ValueReply - class
        value: object
    12: ch.softappeal.yass2.core.remote.ExceptionReply - class
        exception: object
    13: ch.softappeal.yass2.coroutines.session.Packet - class
        requestNumber: 3
        message: object
*/
public fun createStringEncoders(): kotlin.collections.List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>> = listOf(
    ch.softappeal.yass2.core.serialize.string.IntStringEncoder,
    ch.softappeal.yass2.tutorial.MyDateEncoder,
    ch.softappeal.yass2.core.serialize.string.EnumStringEncoder(
        ch.softappeal.yass2.tutorial.Gender::class,
        ch.softappeal.yass2.tutorial.Gender::valueOf,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.tutorial.Address::class, true,
        { i ->
            writeProperty("street", i.street, 0)
            startBodyProperties()
            writeProperty("number", i.number, 3)
        },
        {
            ch.softappeal.yass2.tutorial.Address(
                getProperty("street") as kotlin.String,
            ).apply {
                number = getProperty("number") as kotlin.Int?
            }
        },
        "street" to -1,
        "number" to 3,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.tutorial.Person::class, false,
        { i ->
            writeProperty("name", i.name, 0)
            writeProperty("gender", i.gender, 5)
            writeProperty("birthday", i.birthday, 4)
            writeProperty("addresses", i.addresses, 2)
        },
        {
            ch.softappeal.yass2.tutorial.Person(
                getProperty("name") as kotlin.String,
                getProperty("gender") as ch.softappeal.yass2.tutorial.Gender,
                getProperty("birthday") as ch.softappeal.yass2.tutorial.MyDate,
                getProperty("addresses") as kotlin.collections.List<ch.softappeal.yass2.tutorial.Address>,
            )
        },
        "name" to -1,
        "gender" to 5,
        "birthday" to 4,
        "addresses" to -1,
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.tutorial.DivideByZeroException::class, false,
        { i ->
        },
        {
            ch.softappeal.yass2.tutorial.DivideByZeroException(
            )
        },
    ),
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.tutorial.SubClass::class, false,
        { i ->
            writeProperty("baseClassProperty", i.baseClassProperty, 0)
            writeProperty("subClassProperty", i.subClassProperty, 0)
        },
        {
            ch.softappeal.yass2.tutorial.SubClass(
                getProperty("baseClassProperty") as kotlin.String,
                getProperty("subClassProperty") as kotlin.String,
            )
        },
        "baseClassProperty" to -1,
        "subClassProperty" to -1,
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
)
