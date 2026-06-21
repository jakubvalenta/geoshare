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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsPlaceApiInput @Inject constructor(
    private val googleMapsHtmlInput: dagger.Lazy<GoogleMapsHtmlInput>,
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
        // Get API configuration
        val server = serverRepository.getSelectedGoogleMapsPlace() ?: run {
            // Go to HTML parsing, if server is not configured
            next = MatchedInput(googleMapsHtmlInput.get(), match)
            return@parseResult
        }

        // Parse place id
        val googleMapsParseResult = GoogleMapsUriParser.parse(data)
        points = googleMapsParseResult.points
        val lastPoint = points.lastOrNull() ?: return@parseResult
        val placeId = lastPoint.placeId ?: return@parseResult

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
                        url(server.getUrl(placeId, uriQuote))
                        headers {
                            accept(ContentType.Application.Json)
                        }
                    }
                    .execute { response ->
                        response.body<ServerHttpClientFactory.GoogleMapsResult>()
                    }
            }
        } catch (tr: ResponseNetworkException) {
            if (tr.response.status == HttpStatusCode.BadRequest || tr.response.status == HttpStatusCode.NotFound) {
                // Return no points
                return@parseResult
            }
            throw tr
        }

        // Update points
        val point = GCJ02MainlandChinaPoint(
            lat = res.location.latitude,
            lon = res.location.longitude,
            z = lastPoint.z,
            name = lastPoint.name,
            placeId = placeId,
            source = Source.API,
        )
        points = points.dropLast(1).plus(point).toImmutableList()
    }

    override fun toString() = "GoogleMapsPlaceApiInput"
}
