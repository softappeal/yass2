package ch.softappeal.yass2.contract.generated

@Suppress("UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection")
fun generatedRemoteProxyFactoryCreator(tunnel: ch.softappeal.yass2.remote.Tunnel) = object : ch.softappeal.yass2.remote.RemoteProxyFactory {
    suspend operator fun ch.softappeal.yass2.remote.Tunnel.invoke(serviceId: Int, functionId: Int, vararg parameters: Any?): Any? =
        this(ch.softappeal.yass2.remote.Request(serviceId, functionId, listOf(*parameters))).process()

    override fun <S : Any> create(serviceId: ch.softappeal.yass2.remote.ServiceId<S>): S = when (serviceId.id) {
        1 -> object : ch.softappeal.yass2.contract.Calculator {
            override suspend fun add(p1: kotlin.Int, p2: kotlin.Int) = tunnel(1, 0, p1, p2) as kotlin.Int

            override suspend fun divide(p1: kotlin.Int, p2: kotlin.Int) = tunnel(1, 1, p1, p2) as kotlin.Int
        } as S
        2 -> object : ch.softappeal.yass2.contract.Echo {
            override suspend fun delay(p1: kotlin.Int) {
                tunnel(2, 0, p1)
            }

            override suspend fun echo(p1: kotlin.Any?) = tunnel(2, 1, p1)

            override suspend fun echoRequired(p1: kotlin.Any) = tunnel(2, 2, p1) as kotlin.Any

            override suspend fun noParametersNoResult() {
                tunnel(2, 3)
            }
        } as S
        3 -> object : ch.softappeal.yass2.remote.FlowService {
            override suspend fun create(p1: kotlin.Any) = tunnel(3, 0, p1) as kotlin.Int

            override suspend fun next(p1: kotlin.Int) = tunnel(3, 1, p1)
        } as S
        else -> error("no service id ${serviceId.id}")
    }
}

@Suppress("RemoveRedundantQualifierName", "SpellCheckingInspection")
suspend fun generatedInvoker(request: ch.softappeal.yass2.remote.Request, service: ch.softappeal.yass2.remote.Service): Any? {
    val p = request.parameters
    return when (request.serviceId) {
        1 -> {
            val i = service.implementation as ch.softappeal.yass2.contract.Calculator
            when (request.functionId) {
                0 -> i.add(p[0] as kotlin.Int, p[1] as kotlin.Int)
                1 -> i.divide(p[0] as kotlin.Int, p[1] as kotlin.Int)
                else -> ch.softappeal.yass2.remote.missingFunction(request)
            }
        }
        2 -> {
            val i = service.implementation as ch.softappeal.yass2.contract.Echo
            when (request.functionId) {
                0 -> i.delay(p[0] as kotlin.Int)
                1 -> i.echo(p[0])
                2 -> i.echoRequired(p[0] as kotlin.Any)
                3 -> i.noParametersNoResult()
                else -> ch.softappeal.yass2.remote.missingFunction(request)
            }
        }
        3 -> {
            val i = service.implementation as ch.softappeal.yass2.remote.FlowService
            when (request.functionId) {
                0 -> i.create(p[0] as kotlin.Any)
                1 -> i.next(p[0] as kotlin.Int)
                else -> ch.softappeal.yass2.remote.missingFunction(request)
            }
        }
        else -> error("no service id ${request.serviceId}")
    }
}
