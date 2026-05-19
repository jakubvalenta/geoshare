package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleMapsWebViewInputTest : InputTest {
    private val googleMapsUriInput = GoogleMapsUriInput(
        googleMapsHtmlInput = { throw NotImplementedError() },
        googleMapsPlaceApiInput = { throw NotImplementedError() },
        googleMapsPlaceListWebViewInput = { throw NotImplementedError() },
    )
    private val input = GoogleMapsWebViewInput(
        googleMapsUriInput = { googleMapsUriInput },
    )

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(nextStep = NextStep(googleMapsUriInput, "https://maps.google.com/redirected")),
            input.parse("https://maps.google.com/redirected", "https://maps.google.com/original"),
        )
    }
}
