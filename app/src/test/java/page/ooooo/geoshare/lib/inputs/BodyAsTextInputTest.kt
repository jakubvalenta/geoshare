package page.ooooo.geoshare.lib.inputs

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.ResponseNetworkException
import java.net.MalformedURLException
import java.net.URL

class BodyAsTextInputTest {
    val input = object : BodyAsTextInput {
        override val permissionTitleResId get() = throw NotImplementedError()
        override val loadingIndicatorTitleResId get() = throw NotImplementedError()

        override suspend fun parse(
            data: String,
            prevPoints: Points?,
            uriQuote: UriQuote,
            log: ILog,
        ): ParseResult {
            throw NotImplementedError()
        }
    }
    private val log = FakeLog
    private val maxAttempts = 3
    private val uriQuote = FakeUriQuote

    @Test(expected = MalformedURLException::class)
    fun whenMatchIsInvalidURL_throwsMalformedURLException() = runTest {
        val match = "https://[invalid:ipv6]/"
        val lastAttempt = null
        val networkTools = NetworkTools()
        input.getData(
            match,
            networkTools,
            lastAttempt,
            maxAttempts,
            uriQuote,
            log,
        ) { ParseResult() }
    }

    @Test
    fun whenMatchHasScheme_returnsTheResultOfHttpGetBodyAsText() = runTest {
        val match = "https://maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : NetworkTools(log = log) {
            override suspend fun httpGetBodyAsText(
                url: URL,
                lastAttempt: Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = "${url}-data"
        }
        assertEquals(
            ParseResult(nextMatch = "${match}-data"),
            input.getData(
                match,
                networkTools,
                lastAttempt,
                maxAttempts,
                uriQuote,
                log,
            ) { data -> ParseResult(nextMatch = data) }
        )
    }

    @Test
    fun whenMatchHasNoScheme_returnsTheResultOfHttpGetBodyAsTextCalledWithHttpsScheme() = runTest {
        val match = "maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : NetworkTools(log = log) {
            override suspend fun httpGetBodyAsText(
                url: URL,
                lastAttempt: Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = "${url}-data"
        }
        assertEquals(
            ParseResult(nextMatch = "https://${match}-data"),
            input.getData(
                match,
                networkTools,
                lastAttempt,
                maxAttempts,
                uriQuote,
                log,
            ) { data -> ParseResult(nextMatch = data) }
        )
    }

    @Test(expected = ResponseNetworkException::class)
    fun whenHttpGetBodyAsTextThrowsAnException_throwsTheSameException() = runTest {
        val match = "https://maps.google.com/foo"
        val lastAttempt = null
        val networkTools = object : NetworkTools(log = log) {
            override suspend fun httpGetBodyAsText(
                url: URL,
                lastAttempt: Attempt?,
                maxAttempts: Int,
                dispatcher: CoroutineDispatcher,
            ) = throw ResponseNetworkException(HttpStatusCode.NotFound, Exception())
        }
        input.getData(
            match,
            networkTools,
            lastAttempt,
            maxAttempts,
            uriQuote,
            log,
        ) { ParseResult() }
    }
}
