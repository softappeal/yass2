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

package test

public expect fun test.Calculator.proxy(intercept: ch.softappeal.yass2.core.Interceptor): test.Calculator

public expect fun ch.softappeal.yass2.core.remote.ServiceId<test.Calculator>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): test.Calculator

public expect fun ch.softappeal.yass2.core.remote.ServiceId<test.Calculator>.service(implementation: test.Calculator): ch.softappeal.yass2.core.remote.Service

public expect fun binarySerializer(): ch.softappeal.yass2.core.serialize.binary.BinarySerializer

public expect fun stringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>>
