package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import dagger.Lazy
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.headers
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import page.ooooo.geoshare.lib.network.UnknownNetworkException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsAddressApiInput @Inject constructor(
    private val serverHttpClientFactory: ServerHttpClientFactory,
    private val googleMapsHtmlInput: Lazy<GoogleMapsHtmlInput>,
    private val log: Log,
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
        val server = serverRepository.getSelectedGoogleMaps() ?: run {
            // Go to HTML parsing, if server is not configured
            nextStep = NextStep(googleMapsHtmlInput.get(), match)
            return@parseResult
        }
        val client = serverHttpClientFactory.createHttpClient(server).config {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val query = parseQuery(data)
            ?.let { cleanQuery(it) }
            ?: return@parseResult
        val res = try {
            client.use { client ->
                client
                    .prepareRequest {
                        url(server.getUrl(query, uriQuote))
                        headers {
                            accept(ContentType.Application.Json)
                        }
                    }
                    .execute { response ->
                        response.body<ServerHttpClientFactory.GoogleMapsResults>()
                    }
            }
        } catch (tr: UnknownNetworkException) {
            if (tr.cause is JsonConvertException) {
                // Google returns a JSON without the 'results' property when no coordinates are found, so let's do
                // nothing in this case
                // TODO Test GoogleMapsAddressApiInput empty JSON response
                log.i(TAG, "API returned no results")
                return@parseResult
            }
            throw tr
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

    private fun cleanQuery(query: String): String =
        query.replace(Regex("""\s*@$LAT,$LON\s*$"""), "")

    private companion object {
        private const val TAG = "GoogleMapsAddressApiInput"
    }

    override fun toString() = TAG
}
