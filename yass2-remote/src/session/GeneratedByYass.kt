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
    "RedundantUpperBound",
)

package ch.softappeal.yass2.remote.session

public fun ch.softappeal.yass2.remote.session.KeepAlive.proxy(
    interceptor: ch.softappeal.yass2.remote.Interceptor,
): ch.softappeal.yass2.remote.session.KeepAlive =
    object : ch.softappeal.yass2.remote.session.KeepAlive {
        override suspend fun keepAlive(
        ) {
            interceptor(ch.softappeal.yass2.remote.session.KeepAlive::keepAlive, listOf()) {
                this@proxy.keepAlive()
            }
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.session.KeepAlive>.proxy(
    tunnel: ch.softappeal.yass2.remote.Tunnel,
): ch.softappeal.yass2.remote.session.KeepAlive =
    object : ch.softappeal.yass2.remote.session.KeepAlive {
        override suspend fun keepAlive(
        ) {
            tunnel(ch.softappeal.yass2.remote.Request(id, "keepAlive", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.remote.ServiceId<ch.softappeal.yass2.remote.session.KeepAlive>.service(
    implementation: ch.softappeal.yass2.remote.session.KeepAlive,
): ch.softappeal.yass2.remote.Service =
    ch.softappeal.yass2.remote.Service(id) { function, parameters ->
        when (function) {
            "keepAlive" -> implementation.keepAlive(
            )
            else -> error("service '$id' has no function '$function'")
        }
    }
