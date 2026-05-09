package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleMapsWebViewInputTest : InputTest {
    private val input = GoogleMapsWebViewInput

    @Test
    fun parse_returnsNextInput() = runTest {
        assertEquals(
            ParseResult(nextInput = GoogleMapsUriInput, nextMatch = "foo"),
            input.parse("foo"),
        )
    }
}
