package page.ooooo.geoshare.lib.inputs

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.network.FakeNetworkTools
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import java.net.MalformedURLException
import java.net.URL

class HeadLocationHeaderUriInputTest {
    val input = object : HeadLocationHeaderInput {
        override val pattern get() = throw NotImplementedError()
        override val permissionTitleResId get() = throw NotImplementedError()
        override val loadingIndicatorTitleResId get() = throw NotImplementedError()

        override suspend fun parse(
            data: Uri,
            match: String,
            prevPoints: Points?,
            uriQuote: UriQuote,
            log: Log,
        ) = throw NotImplementedError()
    }
    private val log = FakeLog
    private val maxAttempts = 3
    private val uriQuote = FakeUriQuote

    @Test(expected = MalformedURLException::class)
    fun whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        val lastAttempt = null
        val networkTools = FakeNetworkTools()
        input.withData(
            match,
            networkTools,
            lastAttempt,
            maxAttempts,
            uriQuote,
            log,
        ) { ParseResult() }
    }

    @Test
    fun whenMatchHasScheme_returnsTheResultOfHttpHeadLocationHeader() = runTest {
        val match = "https://maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : FakeNetworkTools() {
            override suspend fun httpHeadLocationHeader(
                url: URL,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = "${url}-data"
        }
        assertEquals(
            ParseResult(nextMatch = "${match}-data"),
            input.withData(
                match,
                networkTools,
                lastAttempt,
                maxAttempts,
                uriQuote,
                log,
            ) { data -> ParseResult(nextMatch = data.toString()) }
        )
    }

    @Test
    fun whenMatchHasNoScheme_returnsTheResultOfHttpHeadLocationHeaderCalledWithHttpsScheme() = runTest {
        val match = "maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : FakeNetworkTools() {
            override suspend fun httpHeadLocationHeader(
                url: URL,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = "${url}-data"
        }
        assertEquals(
            ParseResult(nextMatch = "https://${match}-data"),
            input.withData(
                match,
                networkTools,
                lastAttempt,
                maxAttempts,
                uriQuote,
                log,
            ) { data -> ParseResult(nextMatch = data.toString()) }
        )
    }

    @Test
    fun whenHttpHeadLocationHeaderReturnsRelativeUrl_returnsTheResultOfHttpHeadLocationHeaderAsAbsoluteUrl() = runTest {
        val match = "https://maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : FakeNetworkTools() {
            override suspend fun httpHeadLocationHeader(
                url: URL,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = "bar"
        }
        assertEquals(
            ParseResult(nextMatch = "$match/bar"),
            input.withData(
                match,
                networkTools,
                lastAttempt,
                maxAttempts,
                uriQuote,
                log,
            ) { data -> ParseResult(nextMatch = data.toString()) }
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun whenHttpHeadLocationHeaderThrowsAnException_throwsTheSameException() = runTest {
        val match = "https://maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : FakeNetworkTools() {
            override suspend fun httpHeadLocationHeader(
                url: URL,
                lastAttempt: NetworkTools.Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = throw ResponseNetworkException(HttpStatusCode.NotFound, Exception())
        }
        input.withData(
            match,
            networkTools,
            lastAttempt,
            maxAttempts,
            uriQuote,
            log,
        ) { ParseResult() }
    }
}
