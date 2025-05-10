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

package ch.softappeal.yass2.tutorial

public expect fun ch.softappeal.yass2.tutorial.Calculator.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.tutorial.Calculator

public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.tutorial.Calculator

public expect fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.tutorial.Calculator>.service(
    implementation: ch.softappeal.yass2.tutorial.Calculator,
): ch.softappeal.yass2.core.remote.Service

public expect fun createStringEncoders(): List<ch.softappeal.yass2.core.serialize.string.StringEncoder<*>>
