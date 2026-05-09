package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleMapsWebViewInputTest : InputTest {
    private val input = GoogleMapsWebViewInput

    @Test
    fun parse_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(nextInput = GoogleMapsUriInput, nextMatch = "https://maps.google.com/redirected"),
            input.parse("https://maps.google.com/redirected", "https://maps.google.com/original"),
        )
    }
}
