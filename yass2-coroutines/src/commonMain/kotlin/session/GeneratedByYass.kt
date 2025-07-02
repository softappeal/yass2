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

public fun ch.softappeal.yass2.coroutines.session.Heartbeat.proxy(
    intercept: ch.softappeal.yass2.core.Interceptor,
): ch.softappeal.yass2.coroutines.session.Heartbeat = object : ch.softappeal.yass2.coroutines.session.Heartbeat {
    override suspend fun echo(
    ) {
        intercept("echo", listOf()) {
            this@proxy.echo()
        }
    }
}

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.coroutines.session.Heartbeat>.proxy(
    tunnel: ch.softappeal.yass2.core.remote.Tunnel,
): ch.softappeal.yass2.coroutines.session.Heartbeat =
    object : ch.softappeal.yass2.coroutines.session.Heartbeat {
        override suspend fun echo(
        ) {
            tunnel(ch.softappeal.yass2.core.remote.Request(id, "echo", listOf()))
                .process()
        }
    }

public fun ch.softappeal.yass2.core.remote.ServiceId<ch.softappeal.yass2.coroutines.session.Heartbeat>.service(
    implementation: ch.softappeal.yass2.coroutines.session.Heartbeat,
): ch.softappeal.yass2.core.remote.Service =
    ch.softappeal.yass2.core.remote.Service(id) { function, parameters ->
        when (function) {
            "echo" -> implementation.echo(
            )
            else -> error("service '$id' has no function '$function'")
        }
    }
