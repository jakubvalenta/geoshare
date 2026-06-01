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
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import page.ooooo.geoshare.data.local.preferences.CachedServerToken
import page.ooooo.geoshare.data.local.preferences.CachedServerTokenPreference
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.extensions.base64Decode
import page.ooooo.geoshare.lib.extensions.base64Encode
import page.ooooo.geoshare.lib.extensions.verifySignature

class ServerHttpClientFactoryTest {
    private val apiKeyServer = Server(
        name = "Test Server",
        urlTemplate = "https://api.example.com/{q}",
        authType = ServerAuthType.API_KEY,
        apiKey = "test_api_key",
        apiKeyHeader = "X-My-Header",
    )
    private val attestationServer = Server(
        name = "Test Server",
        urlTemplate = "https://api.example.com/{q}",
        authType = ServerAuthType.ATTESTATION,
        challengeUrl = "https://api.example.com/auth/challenge",
        loginUrl = "https://api.example.com/auth/login",
        registerUrl = "https://api.example.com/auth/register",
    )
    private val challenge = "test challenge".toByteArray()
    private val log = FakeLog
    private val query = "Cherbourg, France"
    private val correctToken = "correct token"
    private val incorrectToken = "incorrect token"
    private val newToken = "new token"
    private val uriQuote = FakeUriQuote

    @Test
    fun createHttpClient_apiKey_whenKeyIsCorrect_returnsResponse() = runTest {
        val keyStoreTools = FakeKeyStoreTools()
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                apiKeyServer.getUrl(query, uriQuote) ->
                    if (request.headers["X-My-Header"] == "test_api_key") {
                        respondOk("success")
                    } else {
                        throw NotImplementedError()
                    }

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        val res = factory.createHttpClient(apiKeyServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
        assertEquals("success", res.bodyAsText())
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_apiKey_whenKeyIsIncorrect_throwsException() = runTest {
        val keyStoreTools = FakeKeyStoreTools()
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                apiKeyServer.getUrl(query, uriQuote) ->
                    if (request.headers["X-My-Header"] == "test_api_key") {
                        throw NotImplementedError()
                    } else {
                        respondError(HttpStatusCode.Unauthorized)
                    }

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        factory.createHttpClient(apiKeyServer.copy(apiKey = "spam")).use { client ->
            client.get(apiKeyServer.getUrl(query, uriQuote))
        }
    }

    @Test
    fun createHttpClient_attestation_whenTokenIsCorrect_returnsResponse() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        "Bearer $correctToken" -> respondOk("success")
                        else -> throw NotImplementedError()
                    }

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository: UserPreferencesRepository = mock {
            on { getValue(CachedServerTokenPreference) } doReturn
                CachedServerToken(correctToken, keyStoreTools.getKey()!!.publicKey.encoded.base64Encode())
        }
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        val res = factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
        assertEquals("success", res.bodyAsText())
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndChallengeFails_throwsException() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                attestationServer.challengeUrl ->
                    respondError(HttpStatusCode.Unauthorized)

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
    }

    @Test(expected = ServerResponseNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndChallengeReturns5xx_throwsException() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                attestationServer.challengeUrl ->
                    respondError(HttpStatusCode.InternalServerError)

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
    }

    @Test(expected = ResponseNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginReturns400_throwsException() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                attestationServer.challengeUrl ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                attestationServer.loginUrl ->
                    respondError(HttpStatusCode.BadRequest)

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
    }

    @Test(expected = ServerResponseNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginReturns5xx_throwsException() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                attestationServer.challengeUrl ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                attestationServer.loginUrl ->
                    respondError(HttpStatusCode.InternalServerError)

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
    }

    @Test(expected = UnauthorizedNetworkException::class)
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginFailsAndRegistrationFails_throwsException() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        null -> respondError(HttpStatusCode.Unauthorized)
                        else -> throw NotImplementedError()
                    }

                attestationServer.challengeUrl ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                attestationServer.loginUrl ->
                    respondError(HttpStatusCode.Unauthorized)

                attestationServer.registerUrl ->
                    respondError(HttpStatusCode.Unauthorized)

                else -> throw NotImplementedError()
            }
        }
        val userPreferencesRepository = FakeUserPreferencesRepository()
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
    }

    @Test
    fun createHttpClient_attestation_whenTokenIsMissingAndLoginFailsAndRegistrationSucceeds_returnsResponse() =
        runTest {
            val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    attestationServer.getUrl(query, uriQuote) ->
                        when (request.headers[HttpHeaders.Authorization]) {
                            null -> respondError(HttpStatusCode.Unauthorized)
                            "Bearer $newToken" -> respondOk("success")
                            else -> throw NotImplementedError()
                        }

                    attestationServer.challengeUrl ->
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                    attestationServer.loginUrl -> {
                        val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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

                    attestationServer.registerUrl -> {
                        val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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
            val userPreferencesRepository = FakeUserPreferencesRepository()
            val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
            val res = factory.createHttpClient(attestationServer).use { client ->
                client.get(attestationServer.getUrl(query, uriQuote))
            }
            assertEquals("success", res.bodyAsText())
            assertEquals(
                CachedServerToken(newToken, keyStoreTools.getKey()!!.publicKey.encoded.base64Encode()),
                userPreferencesRepository.getValue(CachedServerTokenPreference),
            )
        }

    @Test
    fun createHttpClient_attestation_whenTokenIsMissingAndPrivateKeyIsNotGeneratedAndRegistrationSucceeds_returnsResponse() =
        runTest {
            val keyStoreTools = FakeKeyStoreTools()
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    attestationServer.getUrl(query, uriQuote) ->
                        when (request.headers[HttpHeaders.Authorization]) {
                            null -> respondError(HttpStatusCode.Unauthorized)
                            "Bearer $newToken" -> respondOk("success")
                            else -> throw NotImplementedError()
                        }

                    attestationServer.challengeUrl ->
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                    attestationServer.loginUrl -> {
                        val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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

                    attestationServer.registerUrl -> {
                        val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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
            val userPreferencesRepository = FakeUserPreferencesRepository()
            val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
            val res = factory.createHttpClient(attestationServer).use { client ->
                client.get(attestationServer.getUrl(query, uriQuote))
            }
            assertEquals("success", res.bodyAsText())
            assertEquals(
                CachedServerToken(newToken, keyStoreTools.getKey()!!.publicKey.encoded.base64Encode()),
                userPreferencesRepository.getValue(CachedServerTokenPreference),
            )
        }

    @Test
    fun createHttpClient_attestation_whenTokenIsIncorrectAndRefreshTokenIsIncorrectAndLoginFailsAndRegistrationSucceeds_returnsResponse() =
        runTest {
            val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
            val engine = MockEngine { request ->
                when (request.url.toString()) {
                    attestationServer.getUrl(query, uriQuote) ->
                        when (request.headers[HttpHeaders.Authorization]) {
                            "Bearer $incorrectToken" -> respondError(HttpStatusCode.Unauthorized)
                            "Bearer $newToken" -> respondOk("success")
                            else -> throw NotImplementedError()
                        }

                    attestationServer.challengeUrl ->
                        respond(
                            Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )

                    attestationServer.loginUrl -> {
                        val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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

                    attestationServer.registerUrl -> {
                        val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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
                on { getValue(CachedServerTokenPreference) } doReturn
                    CachedServerToken(incorrectToken, "incorrect public key".toByteArray().base64Encode())
            }
            val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
            val res = factory.createHttpClient(attestationServer).use { client ->
                client.get(attestationServer.getUrl(query, uriQuote))
            }
            assertEquals("success", res.bodyAsText())
            assertEquals(
                CachedServerToken(newToken, keyStoreTools.getKey()!!.publicKey.encoded.base64Encode()),
                userPreferencesRepository.getValue(CachedServerTokenPreference),
            )
        }

    @Test
    fun createHttpClient_attestation_whenTokenIsIncorrectAndRefreshTokenIsCorrect_returnsResponse() = runTest {
        val keyStoreTools = FakeKeyStoreTools().apply { generateKey() }
        val engine = MockEngine { request ->
            when (request.url.toString()) {
                attestationServer.getUrl(query, uriQuote) ->
                    when (request.headers[HttpHeaders.Authorization]) {
                        "Bearer $incorrectToken" -> respondError(HttpStatusCode.Unauthorized)
                        "Bearer $newToken" -> respondOk("success")
                        else -> throw NotImplementedError()
                    }

                attestationServer.challengeUrl ->
                    respond(
                        Json.encodeToString(ServerHttpClientFactory.ChallengeResponse(challenge = challenge.base64Encode())),
                        HttpStatusCode.OK,
                        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )

                attestationServer.loginUrl -> {
                    val key = keyStoreTools.getKey() ?: throw NotImplementedError()
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
            on { getValue(CachedServerTokenPreference) } doReturn
                CachedServerToken(incorrectToken, keyStoreTools.getKey()!!.publicKey.encoded.base64Encode())
        }
        val factory = ServerHttpClientFactory(engine, keyStoreTools, log, userPreferencesRepository)
        val res = factory.createHttpClient(attestationServer).use { client ->
            client.get(attestationServer.getUrl(query, uriQuote))
        }
        assertEquals("success", res.bodyAsText())
        assertEquals(
            CachedServerToken(newToken, keyStoreTools.getKey()!!.publicKey.encoded.base64Encode()),
            userPreferencesRepository.getValue(CachedServerTokenPreference),
        )
    }
}
