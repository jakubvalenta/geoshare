package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClient
import io.ktor.client.call.DoubleReceiveException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.CachedServerToken
import page.ooooo.geoshare.data.local.preferences.CachedServerTokenPreference
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.android.KeyStoreTools
import page.ooooo.geoshare.lib.extensions.base64Decode
import page.ooooo.geoshare.lib.extensions.base64Encode
import page.ooooo.geoshare.lib.extensions.sign
import javax.inject.Inject

class ServerHttpClientFactory @Inject constructor(
    private val engine: HttpClientEngine,
    private val keyStoreTools: KeyStoreTools,
    private val log: Log = DefaultLog,
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

    @Serializable
    data class GoogleMapsLocation(val latitude: Double, val longitude: Double)

    @Serializable
    data class GoogleMapsResult(val location: GoogleMapsLocation)

    @Serializable
    data class GoogleMapsResults(val results: List<GoogleMapsResult>)

    fun createHttpClient(server: Server): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            configureLogging(log)
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }

            when (server.authType) {
                ServerAuthType.API_KEY ->
                    install(DefaultRequest) {
                        header(server.apiKeyHeader, server.apiKey)
                    }

                ServerAuthType.ATTESTATION ->
                    install(Auth) {
                        bearer {
                            loadTokens {
                                attestationLoadTokens()
                            }
                            refreshTokens {
                                HttpClient(engine) {
                                    expectSuccess = true
                                    configureLogging(log)
                                    setDefaultTimeouts()

                                    install(ContentNegotiation) {
                                        json()
                                    }
                                }.use { client ->
                                    val loginChallenge = response.parseChallenge()
                                        ?: attestationChallenge(client, server.challengeUrl)
                                    attestationLogin(client, loginChallenge, server.loginUrl) ?: run {
                                        val registrationChallenge =
                                            attestationChallenge(client, server.challengeUrl)
                                        attestationRegister(client, registrationChallenge, server.registerUrl)
                                    }
                                }
                            }
                            sendWithoutRequest { true }
                        }
                    }
            }
        }

    suspend fun attestationLoadTokens(): BearerTokens? =
        userPreferencesRepository.getValue(CachedServerTokenPreference)
            ?.let { BearerTokens(it.token, it.publicKey) }

    private suspend fun HttpResponse.parseChallenge(): ByteArray? =
        try {
            body<ChallengeResponse>().challenge.base64Decode()
        } catch (_: NoTransformationFoundException) {
            null
        }

    private suspend fun RefreshTokensParams.attestationChallenge(client: HttpClient, challengeUrl: String): ByteArray =
        try {
            client.post(challengeUrl) {
                markAsRefreshTokenRequest()
            }
        } catch (e: ClientRequestException) {
            with(e.response) {
                log.e(TAG, "Challenge error ${status.value} ${bodyAsErrorMessage()}")
            }
            throw e
        }
            .body<ChallengeResponse>().challenge.base64Decode()

    private suspend fun RefreshTokensParams.attestationLogin(
        client: HttpClient,
        loginChallenge: ByteArray,
        loginUrl: String,
    ): BearerTokens? {
        // Get key
        val key = keyStoreTools.getKey() ?: return null
        val publicKeyBase64 = key.publicKey.encoded.base64Encode()

        // Login
        val loginSignature = key.privateKey.sign(loginChallenge)
        val token = try {
            client.post(loginUrl) {
                markAsRefreshTokenRequest()
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        challenge = loginChallenge.base64Encode(),
                        signature = loginSignature.base64Encode(),
                        publicKey = publicKeyBase64,
                    )
                )
            }
                .body<TokenResponse>().token
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                return null
            }
            with(e.response) {
                log.e(TAG, "Login error ${status.value} ${bodyAsErrorMessage()}")
            }
            throw e
        }
        log.i(TAG, "Login succeeded")
        userPreferencesRepository.setValue(
            CachedServerTokenPreference,
            CachedServerToken(token, publicKeyBase64),
        )
        return BearerTokens(token, publicKeyBase64)
    }

    private suspend fun RefreshTokensParams.attestationRegister(
        client: HttpClient,
        registrationChallenge: ByteArray,
        registerUrl: String,
    ): BearerTokens {
        // Generate key
        val key = keyStoreTools.generateKey()
        val publicKeyBase64 = key.publicKey.encoded.base64Encode()

        // Register
        val registrationSignature = key.privateKey.sign(registrationChallenge)
        val token = try {
            client.post(registerUrl) {
                markAsRefreshTokenRequest()
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        challenge = registrationChallenge.base64Encode(),
                        signature = registrationSignature.base64Encode(),
                        certificateChain = key.certificateChain.map { it.encoded.base64Encode() },
                    )
                )
            }
                .body<TokenResponse>().token
        } catch (e: ClientRequestException) {
            with(e.response) {
                log.e(TAG, "Registration error ${status.value} ${bodyAsErrorMessage()}")
            }
            throw e
        }
        log.i(TAG, "Registration succeeded")
        userPreferencesRepository.setValue(
            CachedServerTokenPreference,
            CachedServerToken(token, publicKeyBase64),
        )
        return BearerTokens(token, publicKeyBase64)
    }

    private suspend fun HttpResponse.bodyAsErrorMessage(): String = try {
        body<ErrorResponse>().message
    } catch (_: DoubleReceiveException) {
        "<double receive>"
    } catch (_: NoTransformationFoundException) {
        "<no transformation found>"
    }

    private companion object {
        private const val TAG = "ServerHttpClient"
    }
}
