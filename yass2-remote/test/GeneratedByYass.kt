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
)

package ch.softappeal.yass2.remote

public fun ch.softappeal.yass2.remote.Calculator.proxy(
    interceptor: ch.softappeal.yass2.remote.Interceptor,
): ch.softappeal.yass2.remote.Calculator =
    object : ch.softappeal.yass2.remote.Calculator {
        override suspend fun add(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ): kotlin.Int {
            return interceptor(ch.softappeal.yass2.remote.Calculator::add, listOf(p1, p2)) {
                this@proxy.add(p1, p2)
            } as kotlin.Int
        }

        override suspend fun delay(
            p1: kotlin.Int,
        ) {
            interceptor(ch.softappeal.yass2.remote.Calculator::delay, listOf(p1)) {
                this@proxy.delay(p1)
            }
        }

        override suspend fun divide(
            p1: kotlin.Int,
            p2: kotlin.Int,
        ): kotlin.Int {
            return interceptor(ch.softappeal.yass2.remote.Calculator::divide, listOf(p1, p2)) {
                this@proxy.divide(p1, p2)
            } as kotlin.Int
        }

        override suspend fun noParametersNoResult(
        ) {
            interceptor(ch.softappeal.yass2.remote.Calculator::noParametersNoResult, listOf()) {
                this@proxy.noParametersNoResult()
            }
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.remote.Calculator =
    object : ch.softappeal.yass2.remote.Calculator {
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

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.Calculator>.service(
    implementation: ch.softappeal.yass2.remote.Calculator,
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

public fun ch.softappeal.yass2.remote.OtherService.proxy(
    interceptor: ch.softappeal.yass2.remote.Interceptor,
): ch.softappeal.yass2.remote.OtherService =
    object : ch.softappeal.yass2.remote.OtherService {
        override suspend fun other(
        ) {
            interceptor(ch.softappeal.yass2.remote.OtherService::other, listOf()) {
                this@proxy.other()
            }
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.OtherService>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.remote.OtherService =
    object : ch.softappeal.yass2.remote.OtherService {
        override suspend fun other(
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "other", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.OtherService>.service(
    implementation: ch.softappeal.yass2.remote.OtherService,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "other" -> implementation.other(
            )
            else -> error("service '$id' has no function '$function'")
        }
    }
