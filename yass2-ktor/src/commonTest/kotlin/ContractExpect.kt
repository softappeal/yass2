package ch.softappeal.yass2

// copied from generated code

expect fun Calculator.proxy(intercept: ch.softappeal.yass2.core.Interceptor): Calculator
expect fun ch.softappeal.yass2.core.remote.ServiceId<Calculator>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): Calculator
expect fun ch.softappeal.yass2.core.remote.ServiceId<Calculator>.service(implementation: Calculator): ch.softappeal.yass2.core.remote.Service

expect fun Echo.proxy(intercept: ch.softappeal.yass2.core.Interceptor): Echo
expect fun ch.softappeal.yass2.core.remote.ServiceId<Echo>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): Echo
expect fun ch.softappeal.yass2.core.remote.ServiceId<Echo>.service(implementation: Echo): ch.softappeal.yass2.core.remote.Service

expect fun createBinarySerializer(): ch.softappeal.yass2.core.serialize.binary.BinarySerializer

expect fun createStringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>>
