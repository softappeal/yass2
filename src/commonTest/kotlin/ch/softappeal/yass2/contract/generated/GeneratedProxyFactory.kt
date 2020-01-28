package ch.softappeal.yass2.contract.generated

@Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection")
object GeneratedProxyFactory : ch.softappeal.yass2.ProxyFactory {
    override fun <S : Any> create(
        service: kotlin.reflect.KClass<S>, implementation: S,
        interceptor: ch.softappeal.yass2.Interceptor, suspendInterceptor: ch.softappeal.yass2.SuspendInterceptor
    ): S = when (service) {
        ch.softappeal.yass2.contract.Calculator::class -> object : ch.softappeal.yass2.contract.Calculator {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun add(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.contract.Calculator::add, arrayOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Calculator).add(p1, p2) } as kotlin.Int
            }

            override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.contract.Calculator::divide, arrayOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Calculator).divide(p1, p2) } as kotlin.Int
            }
        } as S
        ch.softappeal.yass2.contract.Echo::class -> object : ch.softappeal.yass2.contract.Echo {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = false, needsSuspendInterceptor = true)
            }

            override suspend fun delay(p1: kotlin.Int) {
                suspendInterceptor(ch.softappeal.yass2.contract.Echo::delay, arrayOf(p1)) { (implementation as ch.softappeal.yass2.contract.Echo).delay(p1) }
            }

            override suspend fun echo(p1: kotlin.Any?): kotlin.Any? {
                return suspendInterceptor(ch.softappeal.yass2.contract.Echo::echo, arrayOf(p1)) { (implementation as ch.softappeal.yass2.contract.Echo).echo(p1) }
            }

            override suspend fun echoRequired(p1: kotlin.Any): kotlin.Any {
                return suspendInterceptor(ch.softappeal.yass2.contract.Echo::echoRequired, arrayOf(p1)) { (implementation as ch.softappeal.yass2.contract.Echo).echoRequired(p1) } as kotlin.Any
            }

            override suspend fun noParametersNoResult() {
                suspendInterceptor(ch.softappeal.yass2.contract.Echo::noParametersNoResult, emptyArray()) { (implementation as ch.softappeal.yass2.contract.Echo).noParametersNoResult() }
            }
        } as S
        ch.softappeal.yass2.contract.Mixed::class -> object : ch.softappeal.yass2.contract.Mixed {
            init {
                ch.softappeal.yass2.checkInterceptors(interceptor, suspendInterceptor, needsInterceptor = true, needsSuspendInterceptor = true)
            }

            override fun divide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return interceptor(ch.softappeal.yass2.contract.Mixed::divide, arrayOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Mixed).divide(p1, p2) } as kotlin.Int
            }

            override fun noParametersNoResult() {
                interceptor(ch.softappeal.yass2.contract.Mixed::noParametersNoResult, emptyArray()) { (implementation as ch.softappeal.yass2.contract.Mixed).noParametersNoResult() }
            }

            override suspend fun suspendDivide(p1: kotlin.Int, p2: kotlin.Int): kotlin.Int {
                return suspendInterceptor(ch.softappeal.yass2.contract.Mixed::suspendDivide, arrayOf(p1, p2)) { (implementation as ch.softappeal.yass2.contract.Mixed).suspendDivide(p1, p2) } as kotlin.Int
            }
        } as S
        else -> error("no proxy for '$service'")
    }
}
