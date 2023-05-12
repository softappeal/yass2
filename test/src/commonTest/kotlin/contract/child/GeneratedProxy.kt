package ch.softappeal.yass2.contract.child

@Suppress("RedundantSuppression", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "RemoveRedundantQualifierName", "SpellCheckingInspection", "RedundantVisibilityModifier", "KotlinRedundantDiagnosticSuppress")
public fun ch.softappeal.yass2.contract.child.NoSuspend.proxy(
    intercept: ch.softappeal.yass2.Interceptor,
): ch.softappeal.yass2.contract.child.NoSuspend = object : ch.softappeal.yass2.contract.child.NoSuspend {
    override fun x() {
        intercept(ch.softappeal.yass2.contract.child.NoSuspend::x, listOf()) { this@proxy.x() }
    }
}
