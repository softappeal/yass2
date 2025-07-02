package ch.softappeal.yass2.coroutines.session

import ch.softappeal.yass2.core.remote.ServiceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

public interface Heartbeat {
    public suspend fun echo()
}

public val HeartbeatId: ServiceId<Heartbeat> = ServiceId("hb")

internal object HeartbeatImpl : Heartbeat {
    override suspend fun echo() {}
}

public class HeartbeatConfig(
    public val intervalMillis: Long,
    public val timeoutMillis: Long,
) {
    init {
        require(intervalMillis > 0)
        require(timeoutMillis > 0)
    }

    public fun heartbeat(coroutineScope: CoroutineScope, session: Session<out Connection>): Job = coroutineScope.launch {
        val heartbeat = HeartbeatId.proxy(session.clientTunnel)
        while (isActive) {
            try {
                withTimeout(timeoutMillis) { heartbeat.echo() }
            } catch (e: Exception) {
                session.close(e)
                break
            }
            delay(intervalMillis)
        }
    }
}
