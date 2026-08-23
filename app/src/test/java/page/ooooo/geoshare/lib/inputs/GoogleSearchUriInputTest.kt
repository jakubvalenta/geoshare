package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository

class GoogleSearchUriInputTest : InputTest {
    override val resources: Resources = mock {
        on {
            getString(R.string.input_google_search_error_not_supported)
        } doReturn "Google Search links are not supported"
    }
    private val input = FakeInputRepository.googleSearchUriInput

    @Test
    fun match_valid() {
        assertEquals(
            "https://share.google/foo",
            input.match("https://share.google/foo")
        )
        assertEquals(
            "https://www.google.com/share.google?q=foo",
            input.match("https://www.google.com/share.google?q=foo")
        )
        assertEquals(
            "https://www.google.com/search?foo",
            input.match("https://www.google.com/search?foo")
        )
    }

    @Test
    fun parse_returnsWarning() = runBlocking {
        assertEquals(
            ParseResult.Warning(resources.getString(R.string.input_google_search_error_not_supported)),
            input.parse("https://share.google/foo")
        )
    }
}
