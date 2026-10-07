package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.ServerRepository
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.FetchTools
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import page.ooooo.geoshare.lib.network.ServerHttpClientFactory
import page.ooooo.geoshare.lib.uri.Uri
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsPlaceApiInput @Inject constructor(
    private val googleMapsHtmlInput: dagger.Lazy<GoogleMapsHtmlInput>,
    private val serverHttpClientFactory: ServerHttpClientFactory,
    private val serverRepository: ServerRepository,
    private val uriQuote: UriQuote,
) : BasicInput, Input.HasPermission {
    override fun getName(resources: Resources) = resources.getString(R.string.input_google_maps_place_api_name)
    override val group = InputGroup.GOOGLE_MAPS

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        parseResult {
            // Parse URI
            val uri = Uri.parse(match, uriQuote)
            val googleMapsParseResult = GoogleMapsUriParser.parse(uri)
            points = googleMapsParseResult.points

            // Get API configuration
            val server = serverRepository.getSelectedGoogleMapsPlace() ?: run {
                // Go to HTML parsing, if server is not configured
                next = MatchedInput(googleMapsHtmlInput.get(), match)
                return@parseResult
            }

            // Parse place id
            val lastPoint = points.lastOrNull() ?: return@parseResult
            val placeId = lastPoint.placeId ?: return@parseResult

            // Call API
            val client = serverHttpClientFactory.createHttpClient(server)
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
