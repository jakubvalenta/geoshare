package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import dagger.Lazy
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.headers
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.GoogleMapsApiPreference
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsPlaceApiInput @Inject constructor(
    private val apiService: ApiService,
    private val googleMapsHtmlInput: Lazy<GoogleMapsHtmlInput>,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val uriQuote: UriQuote,
) : BasicInput<Uri>, Input.HasPermission {

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    override suspend fun fetch(match: String, block: suspend (Uri) -> ParseResult) =
        block(Uri.parse(match, uriQuote))

    override suspend fun parse(data: Uri, match: String) = parseResult {
        val apiConfig = userPreferencesRepository.getValue(GoogleMapsApiPreference) ?: run {
            // Go to HTML parsing, if API is not configured
            nextStep = NextStep(googleMapsHtmlInput.get(), match)
            return@parseResult
        }
        val client = apiService.createHttpClient(apiConfig)
        val placeId = parsePlaceId(data) ?: return@parseResult
        val res = client.use { client ->
            client
                .prepareRequest {
                    url {
                        appendPathSegments("v1", "google-maps", "geocode", "place", placeId)
                    }
                    headers {
                        accept(ContentType.Application.Json)
                    }
                }
                .execute { response ->
                    response.body<ApiService.GoogleMapsResult>()
                }
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

    override fun toString() = "GoogleMapsPlaceApiInput"
}
