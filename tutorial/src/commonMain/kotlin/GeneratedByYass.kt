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

public fun createStringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>> = listOf(
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
)
