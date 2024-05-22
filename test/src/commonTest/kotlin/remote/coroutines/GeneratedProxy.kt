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

package ch.softappeal.yass2.remote.coroutines

public fun <F, I> ch.softappeal.yass2.remote.coroutines.FlowService<F, I>.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.remote.coroutines.FlowService<F, I> = object : ch.softappeal.yass2.remote.coroutines.FlowService<F, I> {
    override suspend fun cancel(
        p1: kotlin.Int,
    ) {
        suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService<F, I>::cancel, listOf(p1)) {
            this@proxy.cancel(p1)
        }
    }

    override suspend fun create(
        p1: I,
    ): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService<F, I>::create, listOf(p1)) {
            this@proxy.create(p1)
        } as kotlin.Int
    }

    override suspend fun next(
        p1: kotlin.Int,
    ): F? {
        return suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService<F, I>::next, listOf(p1)) {
            this@proxy.next(p1)
        } as F?
    }
}

public fun <F, I> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.coroutines.FlowService<F, I>>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.remote.coroutines.FlowService<F, I> =
    object : ch.softappeal.yass2.remote.coroutines.FlowService<F, I> {
        override suspend fun cancel(
            p1: kotlin.Int,
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1)))
                .process()
        }

        override suspend fun create(
            p1: I,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1)))
                .process() as kotlin.Int

        override suspend fun next(
            p1: kotlin.Int,
        ) =
            tunnel(ch.softappeal.yass2.remote.Request(id, 2, listOf(p1)))
                .process() as F?
    }

public fun <F, I> ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.coroutines.FlowService<F, I>>.service(
    implementation: ch.softappeal.yass2.remote.coroutines.FlowService<F, I>,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.cancel(
                parameters[0] as kotlin.Int,
            )
            1 -> implementation.create(
                parameters[0] as I,
            )
            2 -> implementation.next(
                parameters[0] as kotlin.Int,
            )
            else -> error("service with id $id has no function with id $functionId")
        }
    }
