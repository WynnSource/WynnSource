package fyw.fyi.wynnsource.utils

import fyw.fyi.wynnsource.config.GlobalConfigPage
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetUtils {
    private var dynamicUrl = Url(GlobalConfigPage.reporting.apiEndpoint)

    fun updateBaseUrl(newUrl: String) {
        dynamicUrl = Url(newUrl)
    }

    private val DynamicBaseUrlPlugin = createClientPlugin("DynamicBaseUrlPlugin") {
        onRequest { request, _ ->
            request.url.protocol = dynamicUrl.protocol
            request.url.host = dynamicUrl.host
            request.url.port = dynamicUrl.port
        }
    }

    private val DynamicAuthKeyPlugin = createClientPlugin("DynamicAuthKeyPlugin") {
        onRequest { request, _ ->
            request.headers["X-API-Key"] = GlobalConfigPage.reporting.apiKey
        }
    }

    val baseUrl = GlobalConfigPage.reporting.apiEndpoint
    val sharedEngine = CIO.create()
    val sharedConfig: (HttpClientConfig<*>) -> Unit = {
        it.install(DynamicBaseUrlPlugin)
        it.install(DynamicAuthKeyPlugin)
        it.install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }
    }

}
