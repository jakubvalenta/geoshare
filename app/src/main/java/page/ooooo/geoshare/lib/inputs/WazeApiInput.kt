package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.buildUrl
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.extensions.chunkedPairs
import page.ooooo.geoshare.lib.extensions.removeRepeated
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.configureLogging
import page.ooooo.geoshare.lib.network.rethrowExceptionsAsNetworkException
import page.ooooo.geoshare.lib.network.setDefaultTimeouts
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class WazeApiInput @Inject constructor(
    private val clock: Clock,
    private val engine: HttpClientEngine,
    private val log: Log,
) : BasicInput<String>, Input.HasPermission {
    @Serializable
    private data class CalculatedLocation(
        val latitude: Double,
        val longitude: Double,
        val city: String? = null, // We assume city can be missing
        val street: String? = null, // We assume street can be missing
    )

    @Serializable
    private data class PickUpGetDriverInfo(
        val calculatedLocation: CalculatedLocation? = null,
        val lat: Double? = null,
        val lon: Double? = null,
        val message: String? = null,
        val route: List<Double>? = null,
        val status: String,
    )

    override fun getName(resources: Resources) = resources.getString(R.string.input_waze_api_name)
    override val group = InputGroup.WAZE

    override suspend fun fetch(match: String, block: suspend (String) -> ParseResult): ParseResult =
        block(match)

    override suspend fun parse(data: String, match: String, resources: Resources) = parseResult {
        val info = HttpClient(engine) {
            expectSuccess = true
            configureLogging(log)
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }.use { client ->
            client
                .get("https://www.waze.com/row-rtserver/web/PickUpGetDriverInfo") {
                    buildUrl {
                        parameter("token", data)
                        parameter("getUserInfo", "true")
                        parameter("_", clock.now().toEpochMilliseconds().toString())
                        accept(ContentType.Application.Json)
                    }
                }
                .body<PickUpGetDriverInfo>()
        }
        when (info.status) {
            "error" ->
                if (info.message?.startsWith("invalid token") == true) {
                    warningMessage = resources.getString(R.string.input_waze_warning_route_expired)
                }

            "ok" ->
                if (info.route != null) {
                    points = buildList {
                        if (info.lat != null && info.lon != null) {
                            add(WGS84Point(info.lat, info.lon, source = Source.API))
                        }
                        addAll(
                            info.route
                                .chunkedPairs()
                                .removeRepeated { prev, curr -> prev?.first == curr.first && prev.second == curr.second }
                                .map { (lon, lat) -> WGS84Point(lat, lon, source = Source.API) }
                        )
                        if (info.calculatedLocation != null) {
                            add(
                                WGS84Point(
                                    info.calculatedLocation.latitude, info.calculatedLocation.longitude,
                                    name = listOfNotNull(info.calculatedLocation.street, info.calculatedLocation.city)
                                        .takeIf { it.isNotEmpty() }
                                        ?.joinToString(", "),
                                    source = Source.API,
                                )
                            )
                        }
                    }.toImmutableList()
                } else {
                    warningMessage = resources.getString(R.string.input_waze_warning_route_empty)
                }
        }
    }

    override fun toString() = "WazeApiInput"
}
