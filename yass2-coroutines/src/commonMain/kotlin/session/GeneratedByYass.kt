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

package ch.softappeal.yass2.coroutines.session

public fun ch.softappeal.yass2.coroutines.session.KeepAlive.proxy(intercept: ch.softappeal.yass2.core.Interceptor): ch.softappeal.yass2.coroutines.session.KeepAlive =
    object : ch.softappeal.yass2.coroutines.session.KeepAlive {
        override suspend fun keepAlive(
        ) {
            intercept("keepAlive", listOf()) {
                this@proxy.keepAlive()
            }
        }
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.coroutines.session.KeepAlive>.proxy(tunnel: ch.softappeal.yass2.core.remote.Tunnel): ch.softappeal.yass2.coroutines.session.KeepAlive =
    object : ch.softappeal.yass2.coroutines.session.KeepAlive {
        override suspend fun keepAlive(
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "keepAlive", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.coroutines.session.KeepAlive>.service(implementation: ch.softappeal.yass2.coroutines.session.KeepAlive): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "keepAlive" -> implementation.keepAlive(
            )
            else -> error("service '$id' has no function '$function'")
        }
    }
