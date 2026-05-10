package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.network.FakeNetworkTools
import page.ooooo.geoshare.lib.network.NetworkTools
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import java.net.SocketTimeoutException

class UriInputTest {
    val input = object : UriInput {
        override val pattern = Regex("""(foo)""")

        override suspend fun parse(
            data: Uri,
            match: String,
            prevResult: ParseResult?,
            uriQuote: UriQuote,
            log: Log,
        ) = throw NotImplementedError()
    }
    private val log = FakeLog
    private val lastAttempt = NetworkTools.Attempt(1, SocketTimeoutNetworkException(SocketTimeoutException()))
    private val maxAttempts = 3
    private val networkTools = FakeNetworkTools()
    private val uriQuote = FakeUriQuote

    @Test
    fun match_returnsFirstRegexGroup() {
        assertEquals("foo", input.match("spam foo spam"))
        assertNull(input.match("spam"))
    }

    @Test
    fun withData_whenDataIsValidUrl_returnsUri() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, match)),
            input.withData(
                match = match,
                networkTools = networkTools,
                lastAttempt = lastAttempt,
                maxAttempts = maxAttempts,
                uriQuote = uriQuote,
                log = log,
            ) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun withData_whenDataIsInvalidUrl_returnsUri() = runTest {
        val match = "https://[invalid:ipv6]/"
        assertEquals(
            ParseResult(nextStep = NextStep(DebugUriInput, match)),
            input.withData(
                match = match,
                networkTools = networkTools,
                lastAttempt = lastAttempt,
                maxAttempts = maxAttempts,
                uriQuote = uriQuote,
                log = log,
            ) { data ->
                ParseResult(
                    nextStep = NextStep(DebugUriInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }
}
