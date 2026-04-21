package ch.softappeal.yass2.ktor

import ch.softappeal.yass2.core.remote.Reply
import ch.softappeal.yass2.core.remote.Request
import ch.softappeal.yass2.core.remote.Tunnel
import ch.softappeal.yass2.core.serialize.Serializer
import ch.softappeal.yass2.core.serialize.fromByteArray
import ch.softappeal.yass2.core.serialize.toByteArray
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class BuildRequestCce(
    val buildRequest: HttpRequestBuilder.() -> Unit,
) : AbstractCoroutineContextElement(BuildRequestCce) {
    companion object Key : CoroutineContext.Key<BuildRequestCce>
}

public suspend fun <T> buildRequest(buildRequest: HttpRequestBuilder.() -> Unit, block: suspend CoroutineScope.() -> T): T =
    withContext(BuildRequestCce(buildRequest)) { block() }

internal class HandleResponseCce(
    val handleResponse: HttpResponse.() -> Unit,
) : AbstractCoroutineContextElement(HandleResponseCce) {
    companion object Key : CoroutineContext.Key<HandleResponseCce>
}

public suspend fun <T> handleResponse(handleResponse: HttpResponse.() -> Unit, block: suspend CoroutineScope.() -> T): T =
    withContext(HandleResponseCce(handleResponse)) { block() }

public fun HttpClient.tunnel(url: String, serializer: Serializer): Tunnel = { request ->
    val response = post(url) {
        currentCoroutineContext()[BuildRequestCce]?.buildRequest(this)
        setBody(serializer.toByteArray(request))
    }
    (serializer.fromByteArray(response.bodyAsBytes()) as Reply).apply {
        currentCoroutineContext()[HandleResponseCce]?.handleResponse(response)
    }
}

internal class CallCce(val call: ApplicationCall) : AbstractCoroutineContextElement(CallCce) {
    companion object Key : CoroutineContext.Key<CallCce>
}

public suspend fun call(): ApplicationCall = currentCoroutineContext()[CallCce]!!.call

public fun Route.route(path: String, serializer: Serializer, tunnel: Tunnel) {
    route(path) {
        post {
            withContext(CallCce(call)) {
                val reply = tunnel(serializer.fromByteArray(call.receive(ByteArray::class)) as Request)
                call.respond(serializer.toByteArray(reply))
            }
        }
    }
}
