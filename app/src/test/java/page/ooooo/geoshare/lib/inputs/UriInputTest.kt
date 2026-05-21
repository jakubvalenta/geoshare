package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

class UriInputTest {
    val input = object : UriInput {
        override val uriQuote = FakeUriQuote

        override val pattern = Regex("""(foo)""")

        override suspend fun parse(
            data: Uri,
            match: String,
            prevResult: ParseResult?,
        ) = throw NotImplementedError()
    }
    private val nextInput = FakeInputRepository.osmAndUriInput

    @Test
    fun match_returnsFirstRegexGroup() {
        assertEquals("foo", input.match("spam foo spam"))
        assertNull(input.match("spam"))
    }

    @Test
    fun fetch_whenDataIsValidUrl_returnsUri() = runTest {
        val match = "https://maps.google.com/foo"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, match)),
            input.fetch(match) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }

    @Test
    fun fetch_whenDataIsInvalidUrl_returnsUri() = runTest {
        val match = "https://[invalid:ipv6]/"
        assertEquals(
            ParseResult(nextStep = NextStep(nextInput, match)),
            input.fetch(match) { data ->
                ParseResult(
                    nextStep = NextStep(nextInput, data.toString()) // Store data in nextStep, so we can test it
                )
            }
        )
    }
}
