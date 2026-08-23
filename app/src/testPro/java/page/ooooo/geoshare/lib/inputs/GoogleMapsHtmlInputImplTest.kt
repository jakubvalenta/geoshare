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
    override val resources: Resources = mock {
        on { getString(R.string.conversion_failed_unsupported_source) } doReturn "This link is not supported"
    }
    private val query = "Cherbourg, France"
    private val uriQuote = FakeUriQuote
    private val input = GoogleMapsHtmlInputImpl(uriQuote)

    @Test
    fun parse_whenUriHasPoints_returnsPoints() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(GCJ02MainlandChinaPoint(name = query, source = Source.URI))),
            input.fetchAndParse("https://maps.google.com/?q=$query"),
        )
    }

    @Test
    fun parse_whenUriDoesNotHavePoints_returnsWarning() = runTest {
        assertEquals(
            ParseResult.Warning(resources.getString(R.string.conversion_failed_unsupported_source)),
            input.fetchAndParse("https://maps.google.com/spam"),
        )
    }
}
