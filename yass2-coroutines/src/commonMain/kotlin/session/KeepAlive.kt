package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.core.Proxies
import ch.softappeal.yass2.core.remote.Service
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.remote.tunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

@Proxies(KeepAlive::class)
public interface KeepAlive {
    public suspend fun keepAlive()
}

public val KeepAliveId: ServiceId<KeepAlive> = ServiceId("KeepAlive")

/**
 * Launches a new coroutine that closes [session] if keep-alive fails.
 * Precondition: [Session.serverTunnel] must use [keepAliveTunnel].
 */
public fun <C : Connection> CoroutineScope.launchKeepAlive(session: Session<C>, timeout: Duration, interval: Duration): Job =
    launch {
        session.closeOnException {
            val keepAlive = KeepAliveId.proxy(session.clientTunnel)
            while (true) {
                withTimeout(timeout) { keepAlive.keepAlive() }
                delay(interval)
            }
        }
    }

private object KeepAliveImpl : KeepAlive {
    override suspend fun keepAlive() {
        // empty
    }
}

/** Adds [KeepAlive]. */
public fun keepAliveTunnel(vararg services: Service): Tunnel = tunnel(KeepAliveId.service(KeepAliveImpl), *services)
