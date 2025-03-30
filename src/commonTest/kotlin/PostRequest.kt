package demo

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import io.ktor.http.contentLength

const val LOCAL_HOST = "localhost"
const val PORT = 28947
const val PATH = "/demo"

@Suppress("HttpUrlsUsage")
suspend fun postRequest(httpClientEngineFactory: HttpClientEngineFactory<*>) {
    val client = HttpClient(httpClientEngineFactory)
    val response = client.request("http://$LOCAL_HOST:$PORT$PATH") {
        method = HttpMethod.Post
    }
    println(response.headers)
    checkNotNull(response.contentLength())
    client.close()
}
