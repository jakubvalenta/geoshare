package page.ooooo.geoshare.lib.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeKeyStoreService
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.lib.FakeLog

class ApiServiceTest {
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            "https://geocode.example.com/v1/google-maps/geocode/address/Cherbourg,%20France" ->
                if (request.headers["X-My-Header"] == "test_api_key") {
                    respondOk("test response")
                } else {
                    respondError(HttpStatusCode.Unauthorized)
                }

            else -> throw NotImplementedError()
        }
    }
    private val keyStoreService = FakeKeyStoreService().apply {
        generateKey("".toByteArray())
    }
    private val log = FakeLog
    private val query = "Cherbourg, France"
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_whenAuthTypeIsApiKeyAndEndpointReturns401_throwsNetworkException() = runTest {
        val apiService = ApiService(engine, keyStoreService, log, userPreferencesRepository)
        apiService.createHttpClient(
            baseUrl = "https://geocode.example.com",
            authType = ServerAuthType.API_KEY,
            apiKey = "spam",
            apiKeyHeader = "X-My-Header",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v1", "google-maps", "geocode", "address", query)
                }
            }
        }
    }

    @Test
    fun createHttpClient_whenAuthTypeIsApiKeyAndEndpointReturnsSuccess_returnsResponse() = runTest {
        val apiService = ApiService(engine, keyStoreService, log, userPreferencesRepository)
        val res = apiService.createHttpClient(
            baseUrl = "https://geocode.example.com",
            authType = ServerAuthType.API_KEY,
            apiKey = "test_api_key",
            apiKeyHeader = "X-My-Header",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v1", "google-maps", "geocode", "address", query)
                }
            }
        }
        assertEquals("test response", res.bodyAsText())
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_whenAuthTypeIsAttestationAndChallengeEndpointReturns401_throwsNetworkException() =
        runTest {
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "https://api.example.com/v1/google-maps/geocode/address/Cherbourg,%20France" ->
                        respondError(HttpStatusCode.Unauthorized)

                    "https://api.example.com/v1/auth/challenge" ->
                        respondError(HttpStatusCode.Unauthorized)

                    else -> throw NotImplementedError()
                }
            }
            val apiService = ApiService(engine, keyStoreService, log, userPreferencesRepository)
            val res = apiService.createHttpClient(
                baseUrl = "https://api.example.com",
                authType = ServerAuthType.ATTESTATION,
                apiKey = "",
                apiKeyHeader = "",
            ).use { client ->
                client.get {
                    url {
                        appendPathSegments("v1", "google-maps", "geocode", "address", query)
                    }
                }
            }
            assertEquals("test response", res.bodyAsText())
        }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_whenAuthTypeIsAttestationAndChallengeEndpointReturns401AndRegisterEndpointReturns401_throwsNetworkException() =
        runTest {
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "https://api.example.com/v1/google-maps/geocode/address/Cherbourg,%20France" ->
                        respondError(HttpStatusCode.Unauthorized)

                    "https://api.example.com/v1/auth/challenge" ->
                        respondError(HttpStatusCode.Unauthorized)

                    "https://api.example.com/v1/auth/register" ->
                        respondError(HttpStatusCode.Unauthorized)

                    else -> throw NotImplementedError()
                }
            }
            val apiService = ApiService(engine, keyStoreService, log, userPreferencesRepository)
            val res = apiService.createHttpClient(
                baseUrl = "https://api.example.com",
                authType = ServerAuthType.ATTESTATION,
                apiKey = "",
                apiKeyHeader = "",
            ).use { client ->
                client.get {
                    url {
                        appendPathSegments("v1", "google-maps", "geocode", "address", query)
                    }
                }
            }
            assertEquals("test response", res.bodyAsText())
        }

    @Test(expected = ServerResponseNetworkException::class)
    fun createHttpClient_whenAuthTypeIsAttestationAndChallengeEndpointReturns5xx_throwsNetworkException() =
        runTest {
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "https://api.example.com/v1/google-maps/geocode/address/Cherbourg,%20France" ->
                        respondError(HttpStatusCode.Unauthorized)

                    "https://api.example.com/v1/auth/challenge" -> respondError(HttpStatusCode.InternalServerError)

                    else -> throw NotImplementedError()
                }
            }
            val apiService = ApiService(engine, keyStoreService, log, userPreferencesRepository)
            val res = apiService.createHttpClient(
                baseUrl = "https://api.example.com",
                authType = ServerAuthType.ATTESTATION,
                apiKey = "",
                apiKeyHeader = "",
            ).use { client ->
                client.get {
                    url {
                        appendPathSegments("v1", "google-maps", "geocode", "address", query)
                    }
                }
            }
            assertEquals("test response", res.bodyAsText())
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndLoginEndpointReturns401AndRegisterEndpointReturns401_throwsNetworkException() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndLoginEndpointReturnsSuccess_returnsResponse() = runTest {
        assumeTrue("Not implemented yet", false)
    }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndLoginEndpointReturns5xx_throwsNetworkException() = runTest {
        assumeTrue("Not implemented yet", false)
    }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndChallengeEndpointReturns401AndRegisterEndpointReturnsSuccess_returnsResponseAndStoresNewToken() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndLoginEndpointReturns401AndRegisterEndpointReturnsSuccess_returnsResponseAndStoresNewToken() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndCacheContainsExpiredTokenAndLoginEndpointReturnsSuccess_returnsResponseAndStoresNewToken() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndCacheContainsInvalidTokenAndLoginEndpointReturnsSuccess_returnsResponseAndStoresNewToken() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndCacheContainsValidTokenAndLoginEndpointReturnsSuccess_returnsResponse() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }

    @Test
    fun createHttpClient_whenAuthTypeIsAttestationAndKeyStoreDoesNotContainPrivateKeyAndRegisterEndpointReturnsSuccess_returnsResponseAndStoresNewToken() =
        runTest {
            assumeTrue("Not implemented yet", false)
        }
}
