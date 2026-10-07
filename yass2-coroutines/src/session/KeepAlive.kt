package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.core.remote.Service
import ch.softappeal.yass2.core.remote.ServiceId
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.remote.tunnel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

public interface KeepAlive {
    public suspend fun keepAlive()
}

public val KeepAliveId: ServiceId<KeepAlive> = ServiceId("KeepAlive")

/**
 * Launches a coroutine that closes the session if keep-alive fails.
 * The coroutine terminates if the session is closed.
 * Precondition: [Session.serverTunnel] must use [keepAliveTunnel].
 */
public fun <C : Connection> Session<C>.launchKeepAlive(timeout: Duration, interval: Duration) {
    launch {
        closeOnException {
            val keepAlive = KeepAliveId.proxy(clientTunnel)
            while (true) {
                withTimeout(timeout) { keepAlive.keepAlive() }
                delay(interval)
            }
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
