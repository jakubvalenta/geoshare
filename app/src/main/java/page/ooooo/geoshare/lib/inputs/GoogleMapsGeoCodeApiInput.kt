package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import kotlin.coroutines.CoroutineContext

object GoogleMapsGeoCodeApiInput : BasicInput<GoogleMapsGeoCodeApiInput.GoogleMapsGeoCodeResult>, Input.HasPermission {

    @Serializable
    class GoogleMapsGeoCodeResult(val latitude: Double, val longitude: Double)

    var token: String? = null

    @StringRes
    override val permissionTitleResId = R.string.converter_geo_share_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_geo_share_loading_indicator_title

    override suspend fun withData(
        match: String,
        log: Log,
        httpClient: HttpClient,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (GoogleMapsGeoCodeResult) -> ParseResult,
    ): ParseResult =
        httpClient.config {
            install(ContentNegotiation) {
                json()
            }
            // TODO Set up authentication
            /*
            // Registration challenge
            val registrationChallenge = jsonClient.post("/v1/auth/challenge")
                .body<ChallengeResponse>().challenge.base64Decode()

            // Register
            val registrationSignature = Certs.leafKey.private.sign(registrationChallenge)
            val certificateChain = CertLists.validFactoryProvisioned
            jsonClient.post("/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        challenge = registrationChallenge.base64Encode(),
                        signature = registrationSignature.base64Encode(),
                        certificateChain = certificateChain.map { it.encoded.base64Encode() },
                    )
                )
            }

            // Login challenge
            val loginChallenge = jsonClient.post("/v1/auth/challenge")
                .body<ChallengeResponse>().challenge.base64Decode()

            // Login
            val loginSignature = Certs.leafKey.private.sign(loginChallenge)
            val publicKey = Certs.leafKey.public
            val res = jsonClient.post("/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        challenge = loginChallenge.base64Encode(),
                        signature = loginSignature.base64Encode(),
                        publicKey = publicKey.encoded.base64Encode(),
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, res.status)
            assertEquals(
                Certs.leafKey.public.fingerprint(),
                Tokens.verify(res.body<TokenResponse>().token).subject,
            )

            // Login again
            val res2 = jsonClient.post("/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        challenge = loginChallenge.base64Encode(),
                        signature = loginSignature.base64Encode(),
                        publicKey = publicKey.encoded.base64Encode(),
                    )
                )
            }
             */
        }
            .prepareRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = API_HOST
                    port = API_PORT
                    appendPathSegments("v1", "google-maps", "geocode", "places", match)
                }
                headers {
                    accept(ContentType.Application.Json)
                }
            }
            .execute { response ->
                block(response.body())
            }

    override suspend fun parse(
        data: GoogleMapsGeoCodeResult,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        val prevPoint = prevResult?.points?.lastOrNull()
        points = persistentListOf(
            GCJ02MainlandChinaPoint(
                data.latitude,
                data.longitude,
                name = prevPoint?.name,
                z = prevPoint?.z,
                source = prevPoint?.source ?: Source.API,
            )
        )
    }

    override fun toString() = "GoogleMapsGeoCodeApiInput"

    private const val API_HOST = "10.0.2.2:8080"
    private const val API_PORT = 8080
    private const val KEYSTORE_KEY = ""
}
