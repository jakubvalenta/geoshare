package page.ooooo.geoshare.lib.network

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
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
import page.ooooo.geoshare.data.local.preferences.Authentication
import page.ooooo.geoshare.data.local.preferences.GoogleMapsApiBaseUrlPreference
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
    private val engine: HttpClientEngine,
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

    fun createHttpClient(baseUrl: URL, authentication: Authentication): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            when (authentication) {
                is Authentication.ApiKey -> {
                    install(DefaultRequest) {
                        url(baseUrl.toString())
                        header(authentication.header, authentication.value)
                    }
                }

                is Authentication.Attestation -> {
                    install(DefaultRequest) {
                        url(baseUrl.toString())
                    }
                    install(Auth) {
                        bearer {
                            loadTokens {
                                attestationLoadTokens()
                            }
                            refreshTokens {
                                attestationLogin(baseUrl) ?: attestationRegister(baseUrl)
                            }
                        }
                    }
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }

    suspend fun attestationLoadTokens(): BearerTokens? =
        userPreferencesRepository.getValue(CachedApiTokenPreference)?.let {
            BearerTokens(it.token, it.publicKey)
        }

    private suspend fun attestationLogin(baseUrl: URL): BearerTokens? {
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            install(DefaultRequest) {
                url(baseUrl.toString())
            }
            install(ContentNegotiation) {
                json()
            }
        }.use { client ->
            // Get key
            val privateKeyEntry = getKey() ?: return null
            val privateKey = privateKeyEntry.privateKey
            val publicKey = privateKeyEntry.certificateChain.first().publicKey
            val publicKeyBase64 = publicKey.encoded.base64Encode()

            // Login challenge
            val loginChallenge = try {
                client
                    .post("/v1/auth/challenge")
                    .body<ChallengeResponse>().challenge.base64Decode()
            } catch (e: ClientRequestException) {
                with(e.response) {
                    log.e(TAG, "Login challenge failed: ${status.value} ${bodyAsErrorMessage()}")
                }
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

    private suspend fun attestationRegister(baseUrl: URL): BearerTokens {
        HttpClient(engine) {
            expectSuccess = true
            setDefaultTimeouts()
            rethrowExceptionsAsNetworkException(log)

            install(DefaultRequest) {
                url(baseUrl.toString())
            }
            install(ContentNegotiation) {
                json()
            }
        }.use { client ->
            // Registration challenge
            val registrationChallenge = try {
                client
                    .post("/v1/auth/challenge")
                    .body<ChallengeResponse>().challenge.base64Decode()
            } catch (e: ClientRequestException) {
                with(e.response) {
                    log.e(TAG, "Registration challenge failed: ${status.value} ${bodyAsErrorMessage()}")
                }
                throw e
            }

            // Generate key
            val privateKeyEntry = generateKey(registrationChallenge)
            val privateKey = privateKeyEntry.privateKey
            val publicKey = privateKeyEntry.certificateChain.first().publicKey
            val publicKeyBase64 = publicKey.encoded.base64Encode()

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

    /**
     * Try to get a key from the key store.
     */
    private fun getKey(): KeyStore.PrivateKeyEntry? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = try {
            keyStore.getEntry(KEYSTORE_ALIAS, null)
        } catch (tr: GeneralSecurityException) {
            log.e(TAG, "Error when getting key from the key store", tr)
            return null
        }
        if (entry !is KeyStore.PrivateKeyEntry) {
            log.e(TAG, "Got key from the key store but it's not a private key")
            return null
        }
        return entry
    }

    /**
     * Generate new key and save it in the key store.
     *
     * If there was a corrupt key in the key store, overwrite it.
     */
    private fun generateKey(challenge: ByteArray): KeyStore.PrivateKeyEntry {
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        val params = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setAlgorithmParameterSpec(
                ECGenParameterSpec(@Suppress("SpellCheckingInspection") "secp256r1")
            )
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            // Setting attestation challenge triggers the signing of the certificate by the attestation certificate
            setAttestationChallenge(challenge)
            // Don't use StrongBox, because it's not available on all devices, and we probably don't need that level of
            // security
            build()
        }
        keyPairGenerator.initialize(params)
        keyPairGenerator.generateKeyPair()

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Key generation succeeded but key store entry not found")
    }

    private suspend fun HttpResponse.bodyAsErrorMessage(): String = try {
        body<ErrorResponse>().message
    } catch (_: DoubleReceiveException) {
        "<double receive>"
    } catch (_: NoTransformationFoundException) {
        "<no transformation found>"
    }

    private companion object {
        private const val KEYSTORE_ALIAS = "geoshare_api"
        private const val TAG = "ApiService"
    }
}
