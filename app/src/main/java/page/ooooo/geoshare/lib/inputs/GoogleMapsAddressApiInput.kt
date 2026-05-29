package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import dagger.Lazy
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsAddressApiInput @Inject constructor(
    private val serverHttpClientFactory: ServerHttpClientFactory,
    private val googleMapsHtmlInput: Lazy<GoogleMapsHtmlInput>,
    private val serverRepository: ServerRepository,
    private val uriQuote: UriQuote,
) : BasicInput<Uri>, Input.HasPermission {

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    override suspend fun fetch(match: String, block: suspend (Uri) -> ParseResult) =
        block(Uri.parse(match, uriQuote))

    override suspend fun parse(data: Uri, match: String) = parseResult {
        val server = serverRepository.getSelected() ?: run {
            // Go to HTML parsing, if API is not configured
            nextStep = NextStep(googleMapsHtmlInput.get(), match)
            return@parseResult
        }
        val client = serverHttpClientFactory.createHttpClient(
            baseUrl = server.baseUrl,
            authType = server.authType,
            apiKey = server.apiKey,
            apiKeyHeader = server.apiKeyHeader,
        ).config {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val query = parseQuery(data) ?: return@parseResult
        val res = client.use { client ->
            client
                .prepareRequest {
                    url {
                        appendPathSegments("v4", "geocode", "address", query)
                    }
                    headers {
                        accept(ContentType.Application.Json)
                    }
                }
                .execute { response ->
                    response.body<ServerHttpClientFactory.GoogleMapsResults>()
                }
        }
        points = res.results
            // Take only the highest ranked result
            .take(1)
            .map { result ->
                GCJ02MainlandChinaPoint(
                    result.location.latitude,
                    result.location.longitude,
                    name = query,
                    source = Source.API
                )
            }.toImmutableList()
    }

    private fun parseQuery(uri: Uri): String? = uri.run {
        // API directions
        // https://www.google.com/maps/dir/?origin={name}&destination={name}
        // API search
        // https://maps.google.com/?q={name}
        listOf(
            "destination",
            @Suppress("SpellCheckingInspection") "daddr",
            "q",
            "query",
        )
            .firstNotNullOfOrNull { key -> Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull() }
            ?.let {
                return it
            }

        val parts = pathParts.dropWhile { it.isEmpty() || it == "maps" }
        val firstPart = parts.firstOrNull()
        when (firstPart) {
            // Directions
            // https://www.google.com/maps/place/{point}/{point}/@{centerX},{centerY},{centerZ}
            "dir" ->
                // Take as query the last path part that isn't a map center or data parameter
                parts.drop(1).lastOrNull { !it.startsWith('@') && !it.startsWith("data=") }

            // Place
            // https://www.google.com/maps/place/{name}/@{centerX},{centerY},{centerZ}
            // Search
            // https://www.google.com/maps/search/{query}
            "place", "search" ->
                // Take as query the second path part
                parts.getOrNull(1)

            else -> null
        }
    }

    override fun toString() = "GoogleMapsAddressApiInput"
}
