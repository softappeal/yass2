package ch.softappeal.yass2.tutorial.contract.generated

@Suppress("RedundantSuppression", "UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier")
public object GeneratedProxyFactory : ch.softappeal.yass2.ProxyFactory {
    override fun <S : Any> create(
        service: kotlin.reflect.KClass<S>, implementation: S,
        interceptor: ch.softappeal.yass2.Interceptor, suspendInterceptor: ch.softappeal.yass2.SuspendInterceptor,
    ): S = when (service) {
        ch.softappeal.yass2.tutorial.contract.Calculator::class -> object : ch.softappeal.yass2.tutorial.contract.Calculator {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun add(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.tutorial.contract.Calculator::add, listOf(p1, p2)) { (implementation as ch.softappeal.yass2.tutorial.contract.Calculator).add(p1, p2) } as kotlin.Int
            }

            override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.tutorial.contract.Calculator::divide, listOf(p1, p2)) { (implementation as ch.softappeal.yass2.tutorial.contract.Calculator).divide(p1, p2) } as kotlin.Int
            }
        } as S
        ch.softappeal.yass2.tutorial.contract.NewsListener::class -> object : ch.softappeal.yass2.tutorial.contract.NewsListener {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun notify(p1: kotlin.String) {
                suspendInterceptor(ch.softappeal.yass2.tutorial.contract.NewsListener::notify, listOf(p1)) { (implementation as ch.softappeal.yass2.tutorial.contract.NewsListener).notify(p1) }
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
        else -> error("no proxy for '$service'")
    }
}
