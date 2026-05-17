package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ApiEndpointPreference
import page.ooooo.geoshare.data.local.preferences.CachedApiTokenPreference
import page.ooooo.geoshare.lib.extensions.base64Decode
import page.ooooo.geoshare.lib.extensions.base64Encode
import java.net.URL
import javax.inject.Inject

class ApiClient @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    @Serializable
    data class ChallengeResponse(val challenge: String)

    @Serializable
    data class RegisterRequest(val challenge: String, val signature: String, val certificateChain: List<String>)

    @Serializable
    data class LoginRequest(val challenge: String, val signature: String, val publicKey: String)

    sealed interface AuthenticationResponse

    @Serializable
    data class ErrorResponse(val message: String) : AuthenticationResponse

    @Serializable
    data class TokenResponse(val token: String) : AuthenticationResponse

    fun configHttpClient(httpClient: HttpClient): HttpClient =
        httpClient.config {
            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        this@ApiClient.loadTokens()
                    }
                    refreshTokens {
                        this@ApiClient.refreshTokens(httpClient)
                    }
                }
            }
        }

    suspend fun getEndpoint(): URL =
        userPreferencesRepository.getValue(ApiEndpointPreference)

    suspend fun loadTokens(): BearerTokens? =
        userPreferencesRepository.getValue(CachedApiTokenPreference)?.let {
            BearerTokens(it.token, it.publicKey)
        }

    suspend fun refreshTokens(httpClient: HttpClient): BearerTokens? {
        // Login challenge
        val loginChallenge = httpClient.post("/v1/auth/challenge")
            .body<ChallengeResponse>().challenge.base64Decode()

        // Login
        val loginSignature = Certs.leafKey.private.sign(loginChallenge)
        val publicKey = Certs.leafKey.public
        val res = httpClient.post("/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    challenge = loginChallenge.base64Encode(),
                    signature = loginSignature.base64Encode(),
                    publicKey = publicKey.encoded.base64Encode(),
                )
            )
        }
        if (res.status.isSuccess()) {
            val token = res.body<TokenResponse>().token
            return BearerTokens(token, publicKey)
        }
        if (res.status == HttpStatusCode.Unauthorized) {
            // Registration challenge
            val registrationChallenge = httpClient.post("/v1/auth/challenge")
                .body<ChallengeResponse>().challenge.base64Decode()

            // Register
            val registrationSignature = Certs.leafKey.private.sign(registrationChallenge)
            val certificateChain = CertLists.validFactoryProvisioned
            val res = httpClient.post("/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        challenge = registrationChallenge.base64Encode(),
                        signature = registrationSignature.base64Encode(),
                        certificateChain = certificateChain.map { it.encoded.base64Encode() },
                    )
                )
            }
            if (res.status.isSuccess()) {
                val token = res.body<TokenResponse>().token
                return BearerTokens(token, publicKey)
            }
        }
        // TODO Log
        return null
    }
}
