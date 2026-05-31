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
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.database.ServerAuthType
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

    fun createHttpClient(
        authType: ServerAuthType,
        apiKey: String,
        apiKeyHeader: String,
        challengeUrl: String,
        loginUrl: String,
        registerUrl: String,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            when (authType) {
                ServerAuthType.API_KEY -> {
                    install(DefaultRequest) {
                        header(apiKeyHeader, apiKey)
                    }
                }

                ServerAuthType.ATTESTATION -> {
                    install(Auth) {
                        bearer {
                            loadTokens {
                                attestationLoadTokens()
                            }
                            refreshTokens {
                                attestationLogin(challengeUrl, loginUrl)
                                    ?: attestationRegister(challengeUrl, registerUrl)
                            }
                        }
                    }
                }
            }
        }

    suspend fun attestationLoadTokens(): BearerTokens? =
        userPreferencesRepository.getValue(CachedServerTokenPreference)?.let {
            BearerTokens(it.token, it.publicKey)
        }

    private suspend fun attestationLogin(challengeUrl: String, loginUrl: String): BearerTokens? {
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()

            install(ContentNegotiation) {
                json()
            }
        }.use { client ->
            // Get key
            val key = keyStoreTools.getKey() ?: return null
            val publicKeyBase64 = key.publicKey.encoded.base64Encode()

            // Login challenge
            val loginChallenge = try {
                client
                    .post(challengeUrl)
                    .body<ChallengeResponse>().challenge.base64Decode()
            } catch (e: ClientRequestException) {
                with(e.response) {
                    log.e(TAG, "Login challenge failed: ${status.value} ${bodyAsErrorMessage()}")
                }
                throw e
            }

            // Login
            val loginSignature = key.privateKey.sign(loginChallenge)
            val token = try {
                client
                    .post(loginUrl) {
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
                    log.e(TAG, "Login failed: ${status.value} ${bodyAsErrorMessage()}")
                }
                throw e
            }
            log.i(TAG, "Login succeeded")
            return BearerTokens(token, publicKeyBase64)
        }
    }

    private suspend fun attestationRegister(challengeUrl: String, registerUrl: String): BearerTokens {
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()

            install(ContentNegotiation) {
                json()
            }
        }.use { client ->
            // Registration challenge
            val registrationChallenge = try {
                client
                    .post(challengeUrl)
                    .body<ChallengeResponse>().challenge.base64Decode()
            } catch (e: ClientRequestException) {
                with(e.response) {
                    log.e(TAG, "Registration challenge failed: ${status.value} ${bodyAsErrorMessage()}")
                }
                throw e
            }

            // Generate key
            val key = keyStoreTools.generateKey()
            val publicKeyBase64 = key.publicKey.encoded.base64Encode()

            // Register
            val registrationSignature = key.privateKey.sign(registrationChallenge)
            val token = try {
                client
                    .post(registerUrl) {
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
                    log.e(TAG, "Registration failed: ${status.value} ${bodyAsErrorMessage()}")
                }
                throw e
            }
            log.i(TAG, "Registration succeeded")
            return BearerTokens(token, publicKeyBase64)
        }
    }

    private suspend fun HttpResponse.bodyAsErrorMessage(): String = try {
        body<ErrorResponse>().message
    } catch (_: DoubleReceiveException) {
        "<double receive>"
    } catch (_: NoTransformationFoundException) {
        "<no transformation found>"
    }

    private companion object {
        private const val TAG = "ServerHttpClientFactory"
    }
}
