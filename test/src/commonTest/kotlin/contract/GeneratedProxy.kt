package ch.softappeal.yass2.contract

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
public fun ch.softappeal.yass2.contract.Calculator.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Calculator = object : ch.softappeal.yass2.contract.Calculator {
    override suspend fun add(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.contract.Calculator::add, listOf(p1, p2)) { this@proxy.add(p1, p2) } as kotlin.Int
    }

    override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.contract.Calculator::divide, listOf(p1, p2)) { this@proxy.divide(p1, p2) } as kotlin.Int
    }
}

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Calculator>.proxy(tunnel: ch.softappeal.yass2.remote.Tunnel): ch.softappeal.yass2.contract.Calculator =
    object : ch.softappeal.yass2.contract.Calculator {
        override suspend fun add(p1: kotlin.Int, p2: kotlin.Int) = tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1, p2))).process() as kotlin.Int

        override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int) = tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1, p2))).process() as kotlin.Int
    }

@Suppress("RedundantSuppression", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "RedundantNullableReturnType")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Calculator>.service(implementation: ch.softappeal.yass2.contract.Calculator): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.add(parameters[0] as kotlin.Int, parameters[1] as kotlin.Int)
            1 -> implementation.divide(parameters[0] as kotlin.Int, parameters[1] as kotlin.Int)
            else -> error("service with id $id has no function with id $functionId")
        }
    }

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
public fun ch.softappeal.yass2.contract.Echo.proxy(
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Echo = object : ch.softappeal.yass2.contract.Echo {
    override suspend fun delay(p1: kotlin.Int) {
        suspendIntercept(ch.softappeal.yass2.contract.Echo::delay, listOf(p1)) { this@proxy.delay(p1) }
    }

    override suspend fun echo(p1: kotlin.Any?): kotlin.Any? {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echo, listOf(p1)) { this@proxy.echo(p1) }
    }

    override suspend fun echoRequired(p1: kotlin.Any): kotlin.Any {
        return suspendIntercept(ch.softappeal.yass2.contract.Echo::echoRequired, listOf(p1)) { this@proxy.echoRequired(p1) } as kotlin.Any
    }

    override suspend fun noParametersNoResult() {
        suspendIntercept(ch.softappeal.yass2.contract.Echo::noParametersNoResult, listOf()) { this@proxy.noParametersNoResult() }
    }
}

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Echo>.proxy(tunnel: ch.softappeal.yass2.remote.Tunnel): ch.softappeal.yass2.contract.Echo =
    object : ch.softappeal.yass2.contract.Echo {
        override suspend fun delay(p1: kotlin.Int) {
            tunnel(ch.softappeal.yass2.remote.Request(id, 0, listOf(p1))).process()
        }

        override suspend fun echo(p1: kotlin.Any?) = tunnel(ch.softappeal.yass2.remote.Request(id, 1, listOf(p1))).process()

        override suspend fun echoRequired(p1: kotlin.Any) = tunnel(ch.softappeal.yass2.remote.Request(id, 2, listOf(p1))).process() as kotlin.Any

        override suspend fun noParametersNoResult() {
            tunnel(ch.softappeal.yass2.remote.Request(id, 3, listOf())).process()
        }
    }

@Suppress("RedundantSuppression", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "RedundantNullableReturnType")
public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.contract.Echo>.service(implementation: ch.softappeal.yass2.contract.Echo): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { functionId, parameters ->
        when (functionId) {
            0 -> implementation.delay(parameters[0] as kotlin.Int)
            1 -> implementation.echo(parameters[0])
            2 -> implementation.echoRequired(parameters[0] as kotlin.Any)
            3 -> implementation.noParametersNoResult()
            else -> error("service with id $id has no function with id $functionId")
        }
    }

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
public fun ch.softappeal.yass2.contract.Mixed.proxy(
    intercept: ch.softappeal.yass2.Interceptor,
    suspendIntercept: ch.softappeal.yass2.SuspendInterceptor,
): ch.softappeal.yass2.contract.Mixed = object : ch.softappeal.yass2.contract.Mixed {
    override fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
        return intercept(ch.softappeal.yass2.contract.Mixed::divide, listOf(p1, p2)) { this@proxy.divide(p1, p2) } as kotlin.Int
    }

    override fun noParametersNoResult() {
        intercept(ch.softappeal.yass2.contract.Mixed::noParametersNoResult, listOf()) { this@proxy.noParametersNoResult() }
    }

    override suspend fun suspendDivide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
        return suspendIntercept(ch.softappeal.yass2.contract.Mixed::suspendDivide, listOf(p1, p2)) { this@proxy.suspendDivide(p1, p2) } as kotlin.Int
    }
}
