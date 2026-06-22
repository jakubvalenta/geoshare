package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class GoogleMapsHtmlInputImplTest : InputTest {
    private val query = "Cherbourg, France"
    private val uriQuote = FakeUriQuote
    private val input = GoogleMapsHtmlInputImpl(uriQuote)
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_unsupported_source) } doReturn "This link is not supported"
    }

    @Test
    fun parse_returnsPointsFromUri() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(name = query, source = Source.URI))),
            input.fetchAndParse("https://maps.google.com/?q=$query"),
        )
    }

    @Test
    fun getErrorMessage_returnsCustomMessage() = runTest {
        assertEquals("This link is not supported", input.getErrorMessage(resources))
    }

    private suspend fun GoogleMapsHtmlInputImpl.fetchAndParse(match: String): ParseResult =
        fetch(match) { data -> parse(data, match) }
}
