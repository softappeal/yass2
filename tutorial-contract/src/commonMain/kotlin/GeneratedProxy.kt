package ch.softappeal.yass2.tutorial.contract

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
public fun ch.softappeal.yass2.tutorial.contract.Calculator.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.tutorial.contract.Calculator = object : ch.softappeal.yass2.tutorial.contract.Calculator {
    override suspend fun add(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.tutorial.contract.Calculator::add, listOf(p1, p2)) { this@proxy.add(p1, p2) } as kotlin.Int
    }

    override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.tutorial.contract.Calculator::divide, listOf(p1, p2)) { this@proxy.divide(p1, p2) } as kotlin.Int
    }
}

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.contract.Calculator>.proxy(tunnel: ch.softappeal.yass2.remote.Tunnel): ch.softappeal.yass2.tutorial.contract.Calculator =
    object : ch.softappeal.yass2.tutorial.contract.Calculator {
        override suspend fun add(p1: kotlin.Int, p2: kotlin.Int) = tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1, p2))).process() as kotlin.Int

        override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int) = tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1, p2))).process() as kotlin.Int
    }

@Suppress("RedundantSuppression", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "RedundantNullableReturnType")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.contract.Calculator>.service(implementation: ch.softappeal.yass2.tutorial.contract.Calculator): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.add(parameters[0] as kotlin.Int, parameters[1] as kotlin.Int)
            1 -> implementation.divide(parameters[0] as kotlin.Int, parameters[1] as kotlin.Int)
            else -> error("service with id $id has no function with id $functionId")
        }
    }

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
public fun ch.softappeal.yass2.tutorial.contract.NewsListener.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.tutorial.contract.NewsListener = object : ch.softappeal.yass2.tutorial.contract.NewsListener {
    override suspend fun notify(p1: kotlin.String) {
        suspendIntercept(ch.softappeal.yass2.tutorial.contract.NewsListener::notify, listOf(p1)) { this@proxy.notify(p1) }
    }
}

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.contract.NewsListener>.proxy(tunnel: ch.softappeal.yass2.remote.Tunnel): ch.softappeal.yass2.tutorial.contract.NewsListener =
    object : ch.softappeal.yass2.tutorial.contract.NewsListener {
        override suspend fun notify(p1: kotlin.String) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1))).process()
        }
    }

@Suppress("RedundantSuppression", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "RedundantNullableReturnType")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.tutorial.contract.NewsListener>.service(implementation: ch.softappeal.yass2.tutorial.contract.NewsListener): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.notify(parameters[0] as kotlin.String)
            else -> error("service with id $id has no function with id $functionId")
        }
    }
