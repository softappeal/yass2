@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "RedundantNullableReturnType",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantSuppression",
)

package ch.softappeal.yass2.remote.coroutines

public fun ch.softappeal.yass2.remote.coroutines.FlowService.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.remote.coroutines.FlowService = object : ch.softappeal.yass2.remote.coroutines.FlowService {
    override suspend fun cancel(p1: kotlin.Int) {
        suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService::cancel, listOf(p1)) { this@proxy.cancel(p1) }
    }

    override suspend fun create(p1: kotlin.Any): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService::create, listOf(p1)) { this@proxy.create(p1) } as kotlin.Int
    }

    override suspend fun next(p1: kotlin.Int): kotlin.Any? {
        return suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService::next, listOf(p1)) { this@proxy.next(p1) } as kotlin.Any?
    }
}

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.coroutines.FlowService>.proxy(tunnel: ch.softappeal.yass2.remote.Tunnel): ch.softappeal.yass2.remote.coroutines.FlowService =
    object : ch.softappeal.yass2.remote.coroutines.FlowService {
        override suspend fun cancel(p1: kotlin.Int) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1))).process()
        }

        override suspend fun create(p1: kotlin.Any) = tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1))).process() as kotlin.Int

        override suspend fun next(p1: kotlin.Int) = tunnel(ch.softappeal.yass2.remote.Request(id, 2, listOf(p1))).process() as kotlin.Any?
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.coroutines.FlowService>.service(implementation: ch.softappeal.yass2.remote.coroutines.FlowService): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.cancel(parameters[0] as kotlin.Int)
            1 -> implementation.create(parameters[0] as kotlin.Any)
            2 -> implementation.next(parameters[0] as kotlin.Int)
            else -> error("service with id $id has no function with id $functionId")
        }
    }
