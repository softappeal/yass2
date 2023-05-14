@file:Suppress(
    "UNCHECKED_CAST",
    "USELESS_CAST",
    "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "unused",
    "RemoveRedundantQualifierName",
    "SpellCheckingInspection",
    "RedundantVisibilityModifier",
    "RedundantNullableReturnType",
    "KotlinRedundantDiagnosticSuppress",
    "RedundantSuppression",
)

package ch.softappeal.yass2.contract.child

public fun ch.softappeal.yass2.contract.child.NoSuspend.proxy(
    intercept: ch.softappeal.yass2.Interceptor,
): ch.softappeal.yass2.contract.child.NoSuspend = object : ch.softappeal.yass2.contract.child.NoSuspend {
    override fun x() {
        intercept(ch.softappeal.yass2.contract.child.NoSuspend::x, listOf()) { this@proxy.x() }
    }
}
