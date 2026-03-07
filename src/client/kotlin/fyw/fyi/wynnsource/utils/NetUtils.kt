package fyw.fyi.wynnsource.utils

import fyw.fyi.wynnsource.WynnSource
import fyw.fyi.wynnsource.WynnSourceClient
import fyw.fyi.wynnsource.config.GlobalConfigPage
import fyw.fyi.wynnsource.server.apis.BetaApi
import fyw.fyi.wynnsource.server.apis.ManagementApi
import fyw.fyi.wynnsource.server.apis.MiscApi
import fyw.fyi.wynnsource.server.apis.PoolApi
import fyw.fyi.wynnsource.utils.StringUtils.isNewerThan
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    val managementClient = ManagementApi(baseUrl, sharedEngine, sharedConfig)
    val poolClient = PoolApi(baseUrl, sharedEngine, sharedConfig)
    val betaClient = BetaApi(baseUrl, sharedEngine, sharedConfig)
    val miscClient = MiscApi(baseUrl, sharedEngine, sharedConfig)

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    @Serializable
    data class GitHubRelease(
        @SerialName("tag_name")
        val tagName: String, // v0.1.0
        val name: String,
        @SerialName("html_url")
        val htmlUrl: String,
        val body: String
    )

    private const val VERSION_CHECK_URL = "https://api.github.com/repos/wynnsource/wynnsource/releases/latest"
    suspend fun checkUpdate(): GitHubRelease? {
        val currentVersion = WynnSource.version?.split("-")[1] // 1.21.11-0.1.0 -> 0.1.0
        try {
            val response = httpClient.get(VERSION_CHECK_URL) {
                timeout {
                    requestTimeoutMillis = 5000
                }
            }.body<GitHubRelease>()
            if (response.tagName.drop(1) isNewerThan currentVersion) {
                return response
            }
            if (WynnSourceClient.isDev)
                return response
        } catch (e: Exception) {
            WynnSource.logger.error("Failed to check for updates", e)
        }

        return null
    }
}
