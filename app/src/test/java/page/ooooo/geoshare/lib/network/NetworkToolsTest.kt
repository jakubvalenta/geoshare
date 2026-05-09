package page.ooooo.geoshare.lib.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.AttributeKey
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.asSource
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.EOFException
import kotlinx.io.buffered
import kotlinx.io.readString
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import page.ooooo.geoshare.lib.FakeLog
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class NetworkToolsTest {
    @Test
    fun httpHeadLocationHeader_requestReturns301WithLocationHeader_callsConnectAndReturnsLocationHeader() =
        runTest {
            val url = URL("https://example.com/")
            val responseHeaders = headersOf(HttpHeaders.Location, "https://example.com/redirect")
            val mockEngine = MockEngine { respond("", HttpStatusCode.MovedPermanently, responseHeaders) }
            val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
            Assert.assertEquals(
                "https://example.com/redirect",
                mockNetworkTools.httpHeadLocationHeader(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler)
                )
            )
            verify(mockNetworkTools).connect(
                engine = eq(mockEngine),
                url = eq(url),
                method = eq(HttpMethod.Head),
                followRedirects = eq(false),
                lastAttempt = eq(null),
                maxAttempts = eq(1),
                block = any(),
            )
        }

    @Test
    fun httpHeadLocationHeader_requestReturns301WithoutLocationHeader_callsConnectAndReturnsNull() =
        runTest {
            val url = URL("https://example.com/")
            val mockEngine = MockEngine { respond("", HttpStatusCode.MovedPermanently) }
            val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
            Assert.assertNull(
                mockNetworkTools.httpHeadLocationHeader(
                    url,
                    dispatcher = StandardTestDispatcher(testScheduler)
                )
            )
            verify(mockNetworkTools).connect(
                engine = eq(mockEngine),
                url = eq(url),
                method = eq(HttpMethod.Head),
                followRedirects = eq(false),
                lastAttempt = eq(null),
                maxAttempts = eq(1),
                block = any(),
            )
        }

    @Test
    fun httpGetBodyAsByteReadChannel_requestReturns200_callsConnectAndReturnsResponseBody() =
        runTest {
            val url = URL("https://example.com/")
            val mockEngine = MockEngine { respond("test content") }
            val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
            val text = mockNetworkTools.httpGetBodyAsByteReadChannel(
                url,
                dispatcher = StandardTestDispatcher(testScheduler)
            ) { source ->
                source.asSource().buffered().readString()
            }
            Assert.assertEquals("test content", text)
            verify(mockNetworkTools).connect(
                engine = eq(mockEngine),
                url = eq(url),
                method = eq(HttpMethod.Get),
                followRedirects = eq(true),
                lastAttempt = eq(null),
                maxAttempts = eq(1),
                block = any(),
            )
        }

    @Test
    fun httpGetRedirectUrlString_requestReturns200_callsConnectAndReturnsRequestUrl() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("") }
        val mockNetworkTools = spy(NetworkTools(mockEngine, log = FakeLog))
        Assert.assertEquals(
            "https://example.com/",
            mockNetworkTools.httpGetRedirectedUrlString(
                url,
                dispatcher = StandardTestDispatcher(testScheduler)
            ),
        )
        verify(mockNetworkTools).connect(
            engine = eq(mockEngine),
            url = eq(url),
            method = eq(HttpMethod.Get),
            followRedirects = eq(true),
            lastAttempt = eq(null),
            maxAttempts = eq(1),
            block = any(),
        )
    }

    @Test
    fun connect_methodIsDefault_makesRequestWithMethodGet() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        Assert.assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        Assert.assertEquals(HttpMethod.Get, lastRequest.method)
    }

    @Test
    fun connect_methodIsGet_makesRequestWithMethodGet() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        Assert.assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                method = HttpMethod.Get,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        Assert.assertEquals(HttpMethod.Get, lastRequest.method)
    }

    @Test
    fun connect_methodIsHead_makesRequestWithMethodHead() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        Assert.assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                method = HttpMethod.Head,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        Assert.assertEquals(HttpMethod.Head, lastRequest.method)
    }

    @Test
    fun connect_followRedirectsIsDefault_makesRequestWithFollowRedirectsTrue() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        Assert.assertEquals(
            "test content",
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig =
            lastRequest.attributes[AttributeKey<HttpClientConfig<*>>("client-config")]
        Assert.assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_followRedirectsIsTrue_makesRequestWithFollowRedirectsTrue() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        Assert.assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                followRedirects = true,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig =
            lastRequest.attributes[io.ktor.util.AttributeKey<HttpClientConfig<*>>("client-config")]
        Assert.assertTrue(clientConfig.followRedirects)
    }

    @Test
    fun connect_followRedirectsIsFalse_makesRequestWithFollowRedirectsFalse() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        Assert.assertEquals(
            "test content",
            mockNetworkTools.connect(
                mockEngine,
                url,
                followRedirects = false,
            ) { response -> response.body<String>() }
        )
        val lastRequest = mockEngine.requestHistory.last()
        val clientConfig =
            lastRequest.attributes[io.ktor.util.AttributeKey<HttpClientConfig<*>>("client-config")]
        Assert.assertFalse(clientConfig.followRedirects)
    }

    @Test
    fun connect_lastAttemptIsNull_doesNotWait() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            Assert.assertEquals(
                "test content",
                mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
            )
        }
        Assert.assertEquals(0.seconds, workDuration)
    }

    @Test
    fun connect_lastAttemptNumberIsOne_doesNotWait() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            Assert.assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    lastAttempt = NetworkTools.Attempt(
                        1,
                        SocketTimeoutNetworkException(SocketTimeoutException())
                    ),
                    maxAttempts = 10,
                ) { response -> response.body<String>() },
            )
        }
        Assert.assertEquals(0.seconds, workDuration)
    }

    @Test
    fun connect_lastAttemptNumberIsTwo_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            Assert.assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    lastAttempt = NetworkTools.Attempt(
                        2,
                        SocketTimeoutNetworkException(SocketTimeoutException())
                    ),
                    maxAttempts = 10,
                ) { response -> response.body<String>() }
            )
        }
        Assert.assertEquals(1.seconds, workDuration)
    }

    @Test
    fun connect_lastAttemptNumberIsThree_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            Assert.assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    lastAttempt = NetworkTools.Attempt(
                        3,
                        SocketTimeoutNetworkException(SocketTimeoutException())
                    ),
                    maxAttempts = 10,
                ) { response -> response.body<String>() }
            )
        }
        Assert.assertEquals(2.seconds, workDuration)
    }

    @Test
    fun connect_lastAttemptNumberIsFour_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            Assert.assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    lastAttempt = NetworkTools.Attempt(
                        4,
                        SocketTimeoutNetworkException(SocketTimeoutException())
                    ),
                    maxAttempts = 10,
                ) { response -> response.body<String>() },
            )
        }
        Assert.assertEquals(4.seconds, workDuration)
    }

    @Test
    fun connect_lastAttemptNumberIsMaxAttempts_waits() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { respond("test content") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        val workDuration = testScheduler.timeSource.measureTime {
            Assert.assertEquals(
                "test content",
                mockNetworkTools.connect(
                    mockEngine,
                    url,
                    lastAttempt = NetworkTools.Attempt(
                        10,
                        SocketTimeoutNetworkException(SocketTimeoutException())
                    ),
                    maxAttempts = 10,
                ) { response -> response.body<String>() },
            )
        }
        Assert.assertEquals(256.seconds, workDuration)
    }

    @Test
    fun connect_lastAttemptNumberIsGreaterThanMaxAttempts_doesNotWaitAndThrowsUnrecoverableException() =
        runTest {
            val url = URL("https://example.com/")
            val mockEngine = MockEngine { respond("") }
            val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
            val workDuration = testScheduler.timeSource.measureTime {
                var threw: Exception? = null
                try {
                    Assert.assertEquals(
                        "test content",
                        mockNetworkTools.connect(
                            mockEngine,
                            url,
                            lastAttempt = NetworkTools.Attempt(
                                11,
                                SocketTimeoutNetworkException(SocketTimeoutException())
                            ),
                            maxAttempts = 10,
                        ) { response -> response.body<String>() },
                    )
                } catch (tr: Exception) {
                    threw = tr
                }
                Assert.assertTrue(threw is UnrecoverableNetworkException)
                Assert.assertTrue(threw is MaxAttemptsReachedNetworkException)
                Assert.assertTrue(threw?.cause is SocketTimeoutNetworkException)
                Assert.assertTrue(threw?.cause?.cause is io.ktor.client.network.sockets.SocketTimeoutException)
            }
            Assert.assertEquals(0.seconds, workDuration)
        }

    @Test
    fun connect_requestReturns2xx_andFollowRedirectsIsDefault_returnsResponseIncludingLocationHeader() =
        runTest {
            val url = URL("https://example.com/")
            for (status in listOf(HttpStatusCode.OK, HttpStatusCode.Created)) {
                val mockEngine = MockEngine {
                    respond(
                        content = "test content",
                        status = status,
                        headers = headersOf(
                            HttpHeaders.Location,
                            "https://example.com/redirect"
                        ),
                    )
                }
                val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
                Assert.assertEquals(
                    "https://example.com/redirect",
                    mockNetworkTools.connect(
                        mockEngine,
                        url,
                    ) { response -> response.headers[HttpHeaders.Location] }
                )
            }
        }

    @Test
    fun connect_requestReturns2xx_andFollowRedirectsIsFalse_returnsResponseIncludingLocationHeader() =
        runTest {
            val url = URL("https://example.com/")
            for (status in listOf(HttpStatusCode.OK, HttpStatusCode.Created)) {
                val mockEngine = MockEngine {
                    respond(
                        content = "test content",
                        status = status,
                        headers = headersOf(
                            HttpHeaders.Location,
                            "https://example.com/redirect"
                        ),
                    )
                }
                val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
                Assert.assertEquals(
                    "https://example.com/redirect",
                    mockNetworkTools.connect(
                        mockEngine,
                        url,
                        followRedirects = false,
                    ) { response -> response.headers[HttpHeaders.Location] }
                )
            }
        }

    @Test
    fun connect_requestReturns3xxAndFollowRedirectsIsDefault_throwsUnrecoverableException() =
        runTest {
            val url = URL("https://example.com/")
            for (status in listOf(
                HttpStatusCode.MovedPermanently,
                HttpStatusCode.Found
            )) {
                val mockEngine = MockEngine { respond("test content", status) }
                val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
                var threw: Exception? = null
                try {
                    mockNetworkTools.connect(
                        mockEngine,
                        url
                    ) { response -> response.body<String>() }
                } catch (tr: Exception) {
                    threw = tr
                }
                Assert.assertTrue(threw is UnrecoverableNetworkException)
                Assert.assertTrue(threw is ResponseNetworkException)
                Assert.assertTrue(threw?.cause is ResponseException)
                Assert.assertFalse(threw?.cause is ServerResponseException)
                val lastRequest = mockEngine.requestHistory.last()
                val clientConfig =
                    lastRequest.attributes[io.ktor.util.AttributeKey<HttpClientConfig<*>>("client-config")]
                Assert.assertEquals(lastRequest.method, HttpMethod.Get)
                Assert.assertTrue(clientConfig.followRedirects)
            }
        }

    @Test
    fun connect_requestReturns3xxAndFollowRedirectsIsFalse_returnsResponseIncludingLocationHeader() =
        runTest {
            val url = URL("https://example.com/")
            for (status in listOf(
                HttpStatusCode.MovedPermanently,
                HttpStatusCode.Found
            )) {
                val mockEngine = MockEngine {
                    respond(
                        content = "test content",
                        status = status,
                        headers = headersOf(
                            HttpHeaders.Location,
                            "https://example.com/redirect"
                        ),
                    )
                }
                val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
                Assert.assertEquals(
                    "https://example.com/redirect",
                    mockNetworkTools.connect(
                        mockEngine,
                        url,
                        followRedirects = false,
                    ) { response -> response.headers[HttpHeaders.Location] }
                )
            }
        }

    @Test
    fun connect_requestReturns4xx_throwsUnrecoverableException() = runTest {
        val url = URL("https://example.com/")
        for (status in listOf(
            HttpStatusCode.BadRequest,
            HttpStatusCode.NotFound,
            HttpStatusCode.TooManyRequests
        )) {
            val mockEngine = MockEngine { respond("test content", status) }
            val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
            var threw: Exception? = null
            try {
                mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
            } catch (tr: Exception) {
                threw = tr
            }
            Assert.assertTrue(threw is UnrecoverableNetworkException)
            Assert.assertTrue(threw is ResponseNetworkException)
            Assert.assertTrue(threw?.cause is ResponseException)
            Assert.assertFalse(threw?.cause is ServerResponseException)
            val lastRequest = mockEngine.requestHistory.last()
            val clientConfig =
                lastRequest.attributes[io.ktor.util.AttributeKey<HttpClientConfig<*>>("client-config")]
            Assert.assertEquals(lastRequest.method, HttpMethod.Get)
            Assert.assertTrue(clientConfig.followRedirects)
        }
    }

    @Test
    fun connect_requestReturns5xx_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")

        for (status in listOf(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.BadGateway
        )) {
            val mockEngine = MockEngine { respond("test content", status) }
            val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
            var threw: Exception? = null
            try {
                mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
            } catch (tr: Exception) {
                threw = tr
            }
            Assert.assertTrue(threw is RecoverableNetworkException)
            Assert.assertTrue(threw is ServerResponseNetworkException)
            Assert.assertTrue(threw?.cause is ServerResponseException)
            val lastRequest = mockEngine.requestHistory.last()
            val clientConfig =
                lastRequest.attributes[io.ktor.util.AttributeKey<HttpClientConfig<*>>("client-config")]
            Assert.assertEquals(lastRequest.method, HttpMethod.Get)
            Assert.assertTrue(clientConfig.followRedirects)
        }
    }

    @Test
    fun connect_requestThrowsUnresolvedAddressException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine =
            MockEngine { throw io.ktor.util.network.UnresolvedAddressException() }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        Assert.assertTrue(threw is RecoverableNetworkException)
        Assert.assertTrue(threw is UnresolvedAddressNetworkException)
        Assert.assertTrue(threw?.cause is UnresolvedAddressException)
    }

    @Test
    fun connect_requestThrowsHttpRequestTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine =
            MockEngine { request -> throw HttpRequestTimeoutException(request) }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        Assert.assertTrue(threw is RecoverableNetworkException)
        Assert.assertTrue(threw is RequestTimeoutNetworkException)
        Assert.assertTrue(threw?.cause is HttpRequestTimeoutException)
    }

    @Test
    fun connect_requestThrowsSocketTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw SocketTimeoutException() }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        Assert.assertTrue(threw is RecoverableNetworkException)
        Assert.assertTrue(threw is SocketTimeoutNetworkException)
        Assert.assertTrue(threw?.cause is SocketTimeoutException)
    }

    @Test
    fun connect_requestThrowsConnectTimeoutException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw ConnectTimeoutException("Connect timeout") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        Assert.assertTrue(threw is RecoverableNetworkException)
        Assert.assertTrue(threw is ConnectTimeoutNetworkException)
        Assert.assertTrue(threw?.cause is ConnectTimeoutException)
    }

    @Test
    fun connect_requestThrowsEOFException_throwsRecoverableException() = runTest {
        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw kotlinx.io.EOFException() }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        Assert.assertTrue(threw is RecoverableNetworkException)
        Assert.assertTrue(threw is ConnectionClosedNetworkException)
        Assert.assertTrue(threw?.cause is EOFException)
    }

    @Test
    fun connect_requestThrowsUnknownException_throwsUnrecoverableException() = runTest {
        class MyException(message: String) : Exception(message)

        val url = URL("https://example.com/")
        val mockEngine = MockEngine { throw MyException("Unknown exception") }
        val mockNetworkTools = NetworkTools(mockEngine, log = FakeLog)
        var threw: Exception? = null
        try {
            mockNetworkTools.connect(mockEngine, url) { response -> response.body<String>() }
        } catch (tr: Exception) {
            threw = tr
        }
        Assert.assertTrue(threw is UnrecoverableNetworkException)
        Assert.assertTrue(threw is UnknownNetworkException)
        Assert.assertTrue(threw?.cause is MyException)
    }
}
