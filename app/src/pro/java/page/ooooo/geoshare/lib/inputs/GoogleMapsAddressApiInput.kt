package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.headers
import kotlinx.collections.immutable.toImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class GoogleMapsAddressApiInput @Inject constructor(
    private val apiService: ApiService,
) : BasicInput<ApiService.GoogleMapsResults>, Input.HasPermission {

    @StringRes
    override val permissionTitleResId = R.string.converter_geo_share_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_geo_share_loading_indicator_title

    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (ApiService.GoogleMapsResults) -> ParseResult,
    ): ParseResult =
        apiService.run {
            createHttpClient(engine)
                .use { client ->
                    client
                        .prepareRequest {
                            url(getEndpoint())
                                .appendPathSegments("v1", "google-maps", "geocode", "address", match)
                            headers {
                                accept(ContentType.Application.Json)
                            }
                        }
                        .execute { response ->
                            block(response.body())
                        }
                }
        }

    override suspend fun parse(
        data: ApiService.GoogleMapsResults,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        val prevPoint = prevResult?.points?.lastOrNull()
        points = data.results
            // Take only the highest ranked result
            .take(1)
            .map { result ->
                GCJ02MainlandChinaPoint(
                    result.location.latitude,
                    result.location.longitude,
                    name = prevPoint?.name,
                    z = prevPoint?.z,
                    source = prevPoint?.source ?: Source.API,
                )
            }.toImmutableList()
    }

    override fun toString() = "GoogleMapsAddressApiInput"
}
