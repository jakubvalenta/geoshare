package page.ooooo.geoshare.data.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.android.MimeType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.TextActivity
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.GCJ02Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.geo.mockGeometries
import page.ooooo.geoshare.data.uri.FakeUriQuote

class SendPointOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val uriQuote = FakeUriQuote
    private val activity = TextActivity(PackageNames.CONVERSATIONS, MimeType.TEXT_PLAIN)
    private val output = SendPointOutput(activity, coordinateConverter)

    @Test
    fun getUriString_whenPointIsWGS84AndWithinMainlandChina_returnsUriWithConvertedCoordinates() {
        assertEquals(
            "https://maps.google.com/?q=31.2285067%2C121.475524",
            output.getUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun getUriString_whenPointIsGCJ02AndWithinMainlandChinaAndLinkSrsIsGCJ02MainlandChina_returnsUriWithUnchangedCoordinates() {
        assertEquals(
            "https://maps.google.com/?q=31.2285069%2C121.4755246",
            output.getUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun getUriString_whenPointDoesNotHaveCoordinates_returnsUriWithSearchQuery() {
        assertEquals(
            "https://maps.google.com/?q=foo%20bar",
            output.getUriString(
                GCJ02Point(name = "foo bar", source = Source.GENERATED),
                uriQuote,
            ),
        )
    }
}
