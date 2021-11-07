package ch.softappeal.yass2.contract.generated

@Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public object ProxyFactory : ch.softappeal.yass2.ProxyFactory {
    override fun <S : Any> create(
        service: kotlin.reflect.KClass<S>, implementation: S,
        interceptor: ch.softappeal.yass2.Interceptor, suspendInterceptor: ch.softappeal.yass2.SuspendInterceptor,
    ): S = when (service) {
        ch.softappeal.yass2.contract.Calculator::class -> object : ch.softappeal.yass2.contract.Calculator {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun add(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.contract.Calculator::add, listOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Calculator).add(p1, p2) } as kotlin.Int
            }

            override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.contract.Calculator::divide, listOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Calculator).divide(p1, p2) } as kotlin.Int
            }
        } as S
        ch.softappeal.yass2.contract.Echo::class -> object : ch.softappeal.yass2.contract.Echo {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun delay(p1: kotlin.Int) {
                suspendInterceptor(ch.softappeal.yass2.contract.Echo::delay, listOf(p1)) { (implementation as ch.softappeal.yass2.contract.Echo).delay(p1) }
            }

            override suspend fun echo(p1: kotlin.Any?): kotlin.Any? {
                return suspendInterceptor(ch.softappeal.yass2.contract.Echo::echo, listOf(p1)) { (implementation as ch.softappeal.yass2.contract.Echo).echo(p1) }
            }

            override suspend fun echoRequired(p1: kotlin.Any): kotlin.Any {
                return suspendInterceptor(ch.softappeal.yass2.contract.Echo::echoRequired, listOf(p1)) { (implementation as ch.softappeal.yass2.contract.Echo).echoRequired(p1) } as kotlin.Any
            }

            override suspend fun noParametersNoResult() {
                suspendInterceptor(ch.softappeal.yass2.contract.Echo::noParametersNoResult, listOf()) { (implementation as ch.softappeal.yass2.contract.Echo).noParametersNoResult() }
            }
        } as S
        ch.softappeal.yass2.remote.coroutines.FlowService::class -> object : ch.softappeal.yass2.remote.coroutines.FlowService {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun cancel(p1: kotlin.Int) {
                suspendInterceptor(ch.softappeal.yass2.remote.coroutines.FlowService::cancel, listOf(p1)) { (implementation as ch.softappeal.yass2.remote.coroutines.FlowService).cancel(p1) }
            }

            override suspend fun create(p1: kotlin.Any): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.remote.coroutines.FlowService::create, listOf(p1)) { (implementation as ch.softappeal.yass2.remote.coroutines.FlowService).create(p1) } as kotlin.Int
            }

            override suspend fun next(p1: kotlin.Int): kotlin.Any? {
                return suspendInterceptor(ch.softappeal.yass2.remote.coroutines.FlowService::next, listOf(p1)) { (implementation as ch.softappeal.yass2.remote.coroutines.FlowService).next(p1) }
            }
        } as S
        ch.softappeal.yass2.contract.Mixed::class -> object : ch.softappeal.yass2.contract.Mixed {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = true, needsSuspendInterceptor = true)
            }

            override fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return interceptor(ch.softappeal.yass2.contract.Mixed::divide, listOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Mixed).divide(p1, p2) } as kotlin.Int
            }

            override fun noParametersNoResult() {
                interceptor(ch.softappeal.yass2.contract.Mixed::noParametersNoResult, listOf()) { (implementation as ch.softappeal.yass2.contract.Mixed).noParametersNoResult() }
            }

            override suspend fun suspendDivide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.contract.Mixed::suspendDivide, listOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Mixed).suspendDivide(p1, p2) } as kotlin.Int
            }
        } as S
        else -> error("no proxy for '$service'")
    }
}
