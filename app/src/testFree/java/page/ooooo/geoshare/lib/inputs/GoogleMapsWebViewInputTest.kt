package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository

class GoogleMapsWebViewInputTest : InputTest {
    private val input = GoogleMapsWebViewInput(
        googleMapsUriInput = { FakeInputRepository.googleMapsUriInput },
    )

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(
                next = MatchedInput(
                    FakeInputRepository.googleMapsUriInput,
                    "https://maps.google.com/redirected"
                )
            ),
            input.parse("https://maps.google.com/redirected", "https://maps.google.com/original"),
        )
    }
}
