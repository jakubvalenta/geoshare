package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import dagger.Lazy
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsPlaceApiInput @Inject constructor(
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
        val server = serverRepository.getSelectedGoogleMapsPlace() ?: run {
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
        val placeId = parsePlaceId(data) ?: return@parseResult
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
        points = persistentListOf(
            GCJ02MainlandChinaPoint(
                res.location.latitude,
                res.location.longitude,
                source = Source.API,
            )
        )
    }

    private fun parsePlaceId(uri: Uri): String? = uri.run {
        Q_PARAM_PATTERN.matchEntire(queryParams["query_place_id"])?.groupOrNull()
    }

    private companion object {
        private const val TAG = "GoogleMapsPlaceApiInput"
    }

    override fun toString() = TAG
}
