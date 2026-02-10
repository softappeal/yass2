@file:Suppress(
    "unused",
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "REDUNDANT_VISIBILITY_MODIFIER",
    "RedundantSuppression",
    "UNUSED_ANONYMOUS_PARAMETER",
    "KotlinRedundantDiagnosticSuppress",
)

package ch.softappeal.yass2

public expect fun ch.softappeal.yass2.Calculator.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.Calculator
public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.Calculator
public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Calculator>.service(implementation: ch.softappeal.yass2.Calculator): ch.softappeal.yass2.core.remote.Service

public expect fun ch.softappeal.yass2.Echo.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.Echo
public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.Echo
public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.Echo>.service(implementation: ch.softappeal.yass2.Echo): ch.softappeal.yass2.core.remote.Service

public expect fun <A, B, C> ch.softappeal.yass2.GenericService<A, B, C>.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.GenericService<A, B, C>
public expect fun <A, B, C> ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.GenericService<A, B, C>>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.GenericService<A, B, C>
public expect fun <A, B, C> ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.GenericService<A, B, C>>.service(implementation: ch.softappeal.yass2.GenericService<A, B, C>): ch.softappeal.yass2.core.remote.Service

public expect fun binarySerializer(): ch.softappeal.yass2.core.serialize.binary.BinarySerializer

public expect fun stringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>>
