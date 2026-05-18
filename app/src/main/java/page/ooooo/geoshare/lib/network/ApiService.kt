package page.ooooo.geoshare.lib.network

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.DoubleReceiveException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ApiEndpointPreference
import page.ooooo.geoshare.data.local.preferences.CachedApiTokenPreference
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.extensions.base64Decode
import page.ooooo.geoshare.lib.extensions.base64Encode
import page.ooooo.geoshare.lib.extensions.sign
import java.net.URL
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject

class ApiService @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val log: Log = DefaultLog,
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

    fun createHttpClient(engine: HttpClientEngine = CIO.create()): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            install(ContentNegotiation) {
                json()
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        this@ApiService.loadTokens()
                    }
                    refreshTokens {
                        login(engine) ?: register(engine)
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

    private suspend fun login(engine: HttpClientEngine): BearerTokens? {
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            install(ContentNegotiation) {
                json()
            }
        }.use { client ->
            val privateKeyEntry = getOrGeneratePrivateKey()
            val privateKey = privateKeyEntry.privateKey
            val publicKey = privateKeyEntry.certificateChain[0].publicKey
            val publicKeyBase64 = publicKey.encoded.base64Encode()

            // Login challenge
            val loginChallenge = try {
                client
                    .post("/v1/auth/challenge")
                    .body<ChallengeResponse>().challenge.base64Decode()
            } catch (e: ResponseNetworkException) {
                logResponseErrorMessage(e.response)
                throw e
            }

            // Login
            val loginSignature = privateKey.sign(loginChallenge)
            val token = try {
                client
                    .post("/v1/auth/login") {
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
            } catch (e: ResponseNetworkException) {
                logResponseErrorMessage(e.response)
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    return null
                }
                throw e
            }
            return BearerTokens(token, publicKeyBase64)
        }
    }

    private suspend fun register(engine: HttpClientEngine): BearerTokens {
        HttpClient(engine) {
            expectSuccess = true
            rethrowExceptionsAsNetworkException(log)
        }.use { client ->
            val privateKeyEntry = getOrGeneratePrivateKey()
            val privateKey = privateKeyEntry.privateKey
            val publicKey = privateKeyEntry.certificateChain[0].publicKey
            val publicKeyBase64 = publicKey.encoded.base64Encode()

            // Registration challenge
            val registrationChallenge = try {
                client.post("/v1/auth/challenge")
                    .body<ChallengeResponse>().challenge.base64Decode()
            } catch (e: ResponseNetworkException) {
                logResponseErrorMessage(e.response)
                throw e
            }

            // Register
            val registrationSignature = privateKey.sign(registrationChallenge)
            val token = try {
                client
                    .post("/v1/auth/register") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            RegisterRequest(
                                challenge = registrationChallenge.base64Encode(),
                                signature = registrationSignature.base64Encode(),
                                certificateChain = privateKeyEntry.certificateChain.map { it.encoded.base64Encode() },
                            )
                        )
                    }
                    .body<TokenResponse>().token
            } catch (e: ResponseNetworkException) {
                logResponseErrorMessage(e.response)
                throw e
            }
            return BearerTokens(token, publicKeyBase64)
        }
    }

    /**
     * See https://developer.android.com/privacy-and-security/keystore
     */
    private fun getOrGeneratePrivateKey(): KeyStore.PrivateKeyEntry {
        // Try to get private key from the key store
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = try {
            keyStore.getEntry(KEYSTORE_ALIAS, null)
        } catch (tr: GeneralSecurityException) {
            log.e(TAG, "Failed to get private key from the key store", tr)
            null
        }
        if (entry is KeyStore.PrivateKeyEntry) {
            return entry
        }

        // Generate new private key; if there was a corrupt key in the key store, it will be overwritten
        log.i(TAG, "Generating new private key")
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        val params = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setAlgorithmParameterSpec(
                ECGenParameterSpec(@Suppress("SpellCheckingInspection") "secp256r1")
            )
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            // Don't use StrongBox, because it's not available on all devices, and we probably don't need that level of
            // security
            build()
        }
        keyPairGenerator.initialize(params)
        keyPairGenerator.generateKeyPair()
        keyStore.load(null) // Make sure the key store is fresh
        return keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Key generation succeeded but key store entry not found")
    }

    private suspend fun logResponseErrorMessage(response: HttpResponse) {
        val message = try {
            response.body<ErrorResponse>().message
        } catch (_: DoubleReceiveException) {
            "<double receive>"
        } catch (_: NoTransformationFoundException) {
            "<no transformation found>"
        }
        log.i(TAG, "Response error $message for ${response.request.url}")
    }

    private companion object {
        private const val KEYSTORE_ALIAS = "geoshare_api"
        private const val TAG = "ApiService"
    }
}
