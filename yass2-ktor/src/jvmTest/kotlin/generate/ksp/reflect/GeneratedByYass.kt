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

package ch.softappeal.yass2.generate.ksp.reflect

public fun ch.softappeal.yass2.generate.ksp.Calculator.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.generate.ksp.Calculator = object : ch.softappeal.yass2.generate.ksp.Calculator {
    override suspend fun add(
        p1: kotlin.Int,
        p2: kotlin.Int,
    ): kotlin.Int {
        return intercept("add", listOf(p1, p2)) {
            this@proxy.add(p1, p2)
        } as kotlin.Int
    }
}

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.generate.ksp.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.generate.ksp.Calculator =
    object : ch.softappeal.yass2.generate.ksp.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "add", listOf(p1, p2)))
                .process() as kotlin.Int
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.generate.ksp.Calculator>.service(
    implementation: ch.softappeal.yass2.generate.ksp.Calculator,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "add" -> implementation.add(
                parameters[0] as kotlin.Int,
                parameters[1] as kotlin.Int,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }

public fun createBinarySerializer(): ch.softappeal.yass2.core.serialize.binary.BinarySerializer =
    object : ch.softappeal.yass2.core.serialize.binary.BinarySerializer() {
        init {
            initialize(
                ch.softappeal.yass2.core.serialize.binary.BinaryEncoder(
                    ch.softappeal.yass2.generate.ksp.A::class,
                    { i ->
                    },
                    {
                        ch.softappeal.yass2.generate.ksp.A(
                        )
                    }
                ),
            )
        }
    }

public fun createStringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>> = listOf(
    ch.softappeal.yass2.core.serialize.string.ClassStringEncoder(
        ch.softappeal.yass2.generate.ksp.A::class, false,
        { i ->
        },
        {
            ch.softappeal.yass2.generate.ksp.A(
            )
        },
    ),
)
