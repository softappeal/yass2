package ch.softappeal.yass2.contract

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
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
        return suspendIntercept(ch.softappeal.yass2.remote.coroutines.FlowService::next, listOf(p1)) { this@proxy.next(p1) }
    }
}

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
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

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public fun ch.softappeal.yass2.contract.NoSuspend.proxy(
    intercept: ch.softappeal.yass2.Interceptor,
): ch.softappeal.yass2.contract.NoSuspend = object : ch.softappeal.yass2.contract.NoSuspend {
    override fun x() {
        intercept(ch.softappeal.yass2.contract.NoSuspend::x, listOf()) { this@proxy.x() }
    }
}
