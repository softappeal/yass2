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

package ch.softappeal.yass2.coroutines.flow

public fun <F, I> ch.softappeal.yass2.coroutines.flow.FlowService<F, I>.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.coroutines.flow.FlowService<F, I> = object : ch.softappeal.yass2.coroutines.flow.FlowService<F, I> {
    override suspend fun cancel(
        p1: kotlin.Int,
    ) {
        intercept("cancel", listOf(p1)) {
            this@proxy.cancel(p1)
        }
    }

    override suspend fun create(
        p1: I,
    ): kotlin.Int {
        return intercept("create", listOf(p1)) {
            this@proxy.create(p1)
        } as kotlin.Int
    }

    override suspend fun next(
        p1: kotlin.Int,
    ): F? {
        return intercept("next", listOf(p1)) {
            this@proxy.next(p1)
        } as F?
    }
}

public fun <F, I> ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.coroutines.flow.FlowService<F, I>>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.coroutines.flow.FlowService<F, I> =
    object : ch.softappeal.yass2.coroutines.flow.FlowService<F, I> {
        override suspend fun cancel(
            p1: kotlin.Int,
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "cancel", listOf(p1)))
                .process()
        }

        override suspend fun create(
            p1: I,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "create", listOf(p1)))
                .process() as kotlin.Int

        override suspend fun next(
            p1: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "next", listOf(p1)))
                .process() as F?
    }

public fun <F, I> ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.coroutines.flow.FlowService<F, I>>.service(
    implementation: ch.softappeal.yass2.coroutines.flow.FlowService<F, I>,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "cancel" -> implementation.cancel(
                parameters[0] as kotlin.Int,
            )
            "create" -> implementation.create(
                parameters[0] as I,
            )
            "next" -> implementation.next(
                parameters[0] as kotlin.Int,
            )
            else -> error("service '$id' has no function '$function'")
        }
    }
