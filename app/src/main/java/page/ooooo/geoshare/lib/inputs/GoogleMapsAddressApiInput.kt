package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import page.ooooo.geoshare.lib.network.UnknownNetworkException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsAddressApiInput @Inject constructor(
    private val googleMapsHtmlInput: dagger.Lazy<GoogleMapsHtmlInput>,
    private val log: Log,
    private val serverHttpClientFactory: ServerHttpClientFactory,
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
        // Parse URI
        val googleMapsParseResult = GoogleMapsUriParser.parse(data)
        points = googleMapsParseResult.points

        // Get API configuration
        val server = serverRepository.getSelectedGoogleMapsAddress() ?: run {
            // Go to HTML parsing, if server is not configured
            next = MatchedInput(googleMapsHtmlInput.get(), match)
            return@parseResult
        }

        // Parse query
        val lastPoint = points.lastOrNull() ?: return@parseResult
        val rawQuery = lastPoint.name?.takeIf { it.isNotEmpty() } ?: return@parseResult

        // Remove trailing coordinates from query
        val query = Regex("""[\s+]*@$LAT$COORD_SEP$LON\s*$""").replace(rawQuery, "")

        // Call API
        val client = serverHttpClientFactory.createHttpClient(server).config {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
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
        } catch (tr: ResponseNetworkException) {
            if (tr.response.status == HttpStatusCode.BadRequest || tr.response.status == HttpStatusCode.NotFound) {
                // Return no points
                return@parseResult
            }
            throw tr
        } catch (tr: UnknownNetworkException) {
            if (tr.cause is JsonConvertException) {
                // Google returns a JSON without the 'results' property when no coordinates are found, so let's silently
                // return no points in this case
                log.i(TAG, "API returned no results")
                return@parseResult
            }
            throw tr
        }

        // Update points
        val highestRankedResult = res.results.firstOrNull() ?: return@parseResult
        val point = GCJ02MainlandChinaPoint(
            lat = highestRankedResult.location.latitude,
            lon = highestRankedResult.location.longitude,
            z = lastPoint.z,
            name = query,
            source = Source.API
        )
        points = points.dropLast(1).plus(point).toImmutableList()
    }

    override fun toString() = TAG

    private companion object {
        private const val TAG = "GoogleMapsAddressApiInput"
    }
}
