package page.ooooo.geoshare.lib.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.di.FakeKeyStoreTools
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.CachedApiToken
import page.ooooo.geoshare.data.local.preferences.CachedApiTokenPreference
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.extensions.base64Decode
import page.ooooo.geoshare.lib.extensions.base64Encode
import page.ooooo.geoshare.lib.extensions.verifySignature

class ServerHttpClientFactoryTest {
    private val apiKeyBaseUrl = "https://geocode.example.com"
    private val attestationBaseUrl = "https://api.example.com"
    private val challenge = "test challenge".toByteArray()
    private val log = FakeLog
    private val query = "Cherbourg, France"
    private val encodedQuery = FakeUriQuote.encode(query, allow = ",")
    private val correctToken = "correct token"
    private val incorrectToken = "incorrect token"
    private val newToken = "new token"
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    @Test
    fun createHttpClient_apiKey_whenKeyIsCorrect_returnsResponse() = runTest {
        val keyStoreService = FakeKeyStoreTools()
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$apiKeyBaseUrl/v4/geocode/address/$encodedQuery" ->
                    if (request.headers["X-My-Header"] == "test_api_key") {
                        respondOk("success")
                    } else {
                        throw NotImplementedError()
                    }

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        val res = factory.createHttpClient(
            baseUrl = apiKeyBaseUrl,
            authType = ServerAuthType.API_KEY,
            apiKey = "test_api_key",
            apiKeyHeader = "X-My-Header",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
        assertEquals("success", res.bodyAsText())
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_apiKey_whenKeyIsIncorrect_throwsException() = runTest {
        val keyStoreService = FakeKeyStoreTools()
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$apiKeyBaseUrl/v4/geocode/address/$encodedQuery" ->
                    if (request.headers["X-My-Header"] == "test_api_key") {
                        throw NotImplementedError()
                    } else {
                        respondError(HttpStatusCode.Unauthorized)
                    }

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        factory.createHttpClient(
            baseUrl = apiKeyBaseUrl,
            authType = ServerAuthType.API_KEY,
            apiKey = "spam",
            apiKeyHeader = "X-My-Header",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
    }

    @Test
    fun createHttpClient_attestation_whenTokenIsCorrect_returnsResponse() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        "Bearer $correctToken" -> respondOk("success")
                        else -> throw NotImplementedError()
                    }

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository: UserPreferencesRepository = mock {
            on { getValue(CachedApiTokenPreference) } doReturn
                CachedApiToken(
                    token = correctToken,
                    publicKey = keyStoreService.getKey()?.publicKey?.encoded?.base64Encode()
                        ?: throw NotImplementedError(),
                )
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        val res = factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
        assertEquals("success", res.bodyAsText())
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndChallengeFails_throwsException() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                "$attestationBaseUrl/v1/auth/challenge" ->
                    respondError(HttpStatusCode.Unauthorized)

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
    }

    @Test(expected = ServerResponseNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndChallengeReturns5xx_throwsException() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                "$attestationBaseUrl/v1/auth/challenge" ->
                    respondError(HttpStatusCode.InternalServerError)

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
    }

    @Test(expected = ResponseNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginReturns400_throwsException() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                "$attestationBaseUrl/v1/auth/challenge" ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                "$attestationBaseUrl/v1/auth/login" ->
                    respondError(HttpStatusCode.BadRequest)

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
    }

    @Test(expected = ServerResponseNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginReturns5xx_throwsException() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                "$attestationBaseUrl/v1/auth/challenge" ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                "$attestationBaseUrl/v1/auth/login" ->
                    respondError(HttpStatusCode.InternalServerError)

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginFailsAndRegistrationFails_throwsException() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                "$attestationBaseUrl/v1/auth/challenge" ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                "$attestationBaseUrl/v1/auth/login" ->
                    respondError(HttpStatusCode.Unauthorized)

                "$attestationBaseUrl/v1/auth/register" ->
                    respondError(HttpStatusCode.Unauthorized)

                else -> throw NotImplementedError()
            }
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
    }

    @Test
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginFailsAndRegistrationSucceeds_returnsResponse() =
        runTest {
            val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                        when (request.headers[HttpHeaders.Authorization]) {
                            null -> respondError(HttpStatusCode.Unauthorized)
                            "Bearer $newToken" -> respondOk("success")
                            else -> throw NotImplementedError()
                        }

                    "$attestationBaseUrl/v1/auth/challenge" ->
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                    "$attestationBaseUrl/v1/auth/login" -> {
                        val key = keyStoreService.getKey() ?: throw NotImplementedError()
                        val body =
                            Json.decodeFromString<ServerHttpClientFactory.LoginRequest>((request.body as TextContent).text)
                        val signatureOk = key.publicKey.verifySignature(
                            body.signature.base64Decode(),
                            body.challenge.base64Decode(),
                        )
                        val publicKeyOk = key.publicKey.encoded.base64Encode() == body.publicKey
                        if (signatureOk && publicKeyOk) {
                            respondError(HttpStatusCode.Unauthorized)
                        } else {
                            throw NotImplementedError()
                        }
                    }

                    "$attestationBaseUrl/v1/auth/register" -> {
                        val key = keyStoreService.getKey() ?: throw NotImplementedError()
                        val body =
                            Json.decodeFromString<ServerHttpClientFactory.RegisterRequest>((request.body as TextContent).text)
                        val signatureOk = key.publicKey.verifySignature(
                            body.signature.base64Decode(),
                            body.challenge.base64Decode(),
                        )
                        val chainOk = key.certificateChain.map { it.encoded.base64Encode() } == body.certificateChain
                        if (signatureOk && chainOk) {
                            respond(
                                Json.encodeToString(ServerHttpClientFactory.TokenResponse(token = newToken)),
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                            )
                        } else {
                            throw NotImplementedError()
                        }
                    }

                    else -> throw NotImplementedError()
                }
            }
            val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
            val res = factory.createHttpClient(
                baseUrl = attestationBaseUrl,
                authType = ServerAuthType.ATTESTATION,
                apiKey = "",
                apiKeyHeader = "",
            ).use { client ->
                client.get {
                    url {
                        appendPathSegments("v4", "geocode", "address", query)
                    }
                }
            }
            assertEquals("success", res.bodyAsText())
            // TODO Test saves new token
        }

    @Test
    fun createHttpClient_attestation_whenTokenIsMissingAndPrivateKeyIsNotGeneratedAndRegistrationSucceeds_returnsResponse() =
        runTest {
            val keyStoreService = FakeKeyStoreTools()
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                        when (request.headers[HttpHeaders.Authorization]) {
                            null -> respondError(HttpStatusCode.Unauthorized)
                            "Bearer $newToken" -> respondOk("success")
                            else -> throw NotImplementedError()
                        }

                    "$attestationBaseUrl/v1/auth/challenge" ->
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                    "$attestationBaseUrl/v1/auth/login" -> {
                        val key = keyStoreService.getKey() ?: throw NotImplementedError()
                        val body =
                            Json.decodeFromString<ServerHttpClientFactory.LoginRequest>((request.body as TextContent).text)
                        val signatureOk = key.publicKey.verifySignature(
                            body.signature.base64Decode(),
                            body.challenge.base64Decode(),
                        )
                        val publicKeyOk = key.publicKey.encoded.base64Encode() == body.publicKey
                        if (signatureOk && publicKeyOk) {
                            throw NotImplementedError()
                        } else {
                            respondError(HttpStatusCode.Unauthorized)
                        }
                    }

                    "$attestationBaseUrl/v1/auth/register" -> {
                        val key = keyStoreService.getKey() ?: throw NotImplementedError()
                        val body =
                            Json.decodeFromString<ServerHttpClientFactory.RegisterRequest>((request.body as TextContent).text)
                        val signatureOk = key.publicKey.verifySignature(
                            body.signature.base64Decode(),
                            body.challenge.base64Decode(),
                        )
                        val chainOk = key.certificateChain.map { it.encoded.base64Encode() } == body.certificateChain
                        if (signatureOk && chainOk) {
                            respond(
                                Json.encodeToString(ServerHttpClientFactory.TokenResponse(token = newToken)),
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                            )
                        } else {
                            throw NotImplementedError()
                        }
                    }

                    else -> throw NotImplementedError()
                }
            }
            val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
            val res = factory.createHttpClient(
                baseUrl = attestationBaseUrl,
                authType = ServerAuthType.ATTESTATION,
                apiKey = "",
                apiKeyHeader = "",
            ).use { client ->
                client.get {
                    url {
                        appendPathSegments("v4", "geocode", "address", query)
                    }
                }
            }
            assertEquals("success", res.bodyAsText())
            // TODO Test saves new token
        }

    @Test
    fun createHttpClient_attestation_whenTokenIsIncorrectAndRefreshTokenIsIncorrectAndLoginFailsAndRegistrationSucceeds_returnsResponse() =
        runTest {
            val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                        when (request.headers[HttpHeaders.Authorization]) {
                            "Bearer $incorrectToken" -> respondError(HttpStatusCode.Unauthorized)
                            "Bearer $newToken" -> respondOk("success")
                            else -> throw NotImplementedError()
                        }

                    "$attestationBaseUrl/v1/auth/challenge" ->
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                    "$attestationBaseUrl/v1/auth/login" -> {
                        val key = keyStoreService.getKey() ?: throw NotImplementedError()
                        val body =
                            Json.decodeFromString<ServerHttpClientFactory.LoginRequest>((request.body as TextContent).text)
                        val signatureOk = key.publicKey.verifySignature(
                            body.signature.base64Decode(),
                            body.challenge.base64Decode(),
                        )
                        val publicKeyOk = key.publicKey.encoded.base64Encode() == body.publicKey
                        if (signatureOk && publicKeyOk) {
                            respondError(HttpStatusCode.Unauthorized)
                        } else {
                            throw NotImplementedError()
                        }
                    }

                    "$attestationBaseUrl/v1/auth/register" -> {
                        val key = keyStoreService.getKey() ?: throw NotImplementedError()
                        val body =
                            Json.decodeFromString<ServerHttpClientFactory.RegisterRequest>((request.body as TextContent).text)
                        val signatureOk = key.publicKey.verifySignature(
                            body.signature.base64Decode(),
                            body.challenge.base64Decode(),
                        )
                        val chainOk = key.certificateChain.map { it.encoded.base64Encode() } == body.certificateChain
                        if (signatureOk && chainOk) {
                            respond(
                                Json.encodeToString(ServerHttpClientFactory.TokenResponse(token = newToken)),
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                            )
                        } else {
                            throw NotImplementedError()
                        }
                    }

                    else -> throw NotImplementedError()
                }
            }
            val userPreferencesRepository: UserPreferencesRepository = mock {
                on { getValue(CachedApiTokenPreference) } doReturn
                    CachedApiToken(
                        token = incorrectToken,
                        publicKey = "incorrect public key".toByteArray().base64Encode(),
                    )
            }
            val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
            val res = factory.createHttpClient(
                baseUrl = attestationBaseUrl,
                authType = ServerAuthType.ATTESTATION,
                apiKey = "",
                apiKeyHeader = "",
            ).use { client ->
                client.get {
                    url {
                        appendPathSegments("v4", "geocode", "address", query)
                    }
                }
            }
            assertEquals("success", res.bodyAsText())
            // TODO Test saves new token
        }

    @Test
    fun createHttpClient_attestation_whenTokenIsIncorrectAndRefreshTokenIsCorrect_returnsResponse() = runTest {
        val keyStoreService = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                "$attestationBaseUrl/v4/geocode/address/$encodedQuery" ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        "Bearer $incorrectToken" -> respondError(HttpStatusCode.Unauthorized)
                        "Bearer $newToken" -> respondOk("success")
                        else -> throw NotImplementedError()
                    }

                "$attestationBaseUrl/v1/auth/challenge" ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                "$attestationBaseUrl/v1/auth/login" -> {
                    val key = keyStoreService.getKey() ?: throw NotImplementedError()
                    val body =
                        Json.decodeFromString<ServerHttpClientFactory.LoginRequest>((request.body as TextContent).text)
                    val signatureOk = key.publicKey.verifySignature(
                        body.signature.base64Decode(),
                        body.challenge.base64Decode(),
                    )
                    val publicKeyOk = key.publicKey.encoded.base64Encode() == body.publicKey
                    if (signatureOk && publicKeyOk) {
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.TokenResponse(token = newToken)),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    } else {
                        throw NotImplementedError()
                    }
                }

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository: UserPreferencesRepository = mock {
            on { getValue(CachedApiTokenPreference) } doReturn
                CachedApiToken(
                    token = incorrectToken,
                    publicKey = keyStoreService.getKey()?.publicKey?.encoded?.base64Encode()
                        ?: throw NotImplementedError(),
                )
        }
        val factory = ServerHttpClientFactory(engine, keyStoreService, log, userPreferencesRepository)
        val res = factory.createHttpClient(
            baseUrl = attestationBaseUrl,
            authType = ServerAuthType.ATTESTATION,
            apiKey = "",
            apiKeyHeader = "",
        ).use { client ->
            client.get {
                url {
                    appendPathSegments("v4", "geocode", "address", query)
                }
            }
        }
        assertEquals("success", res.bodyAsText())
        // TODO Test saves new token
    }
}
