package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.GeoTest
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.lib.geo.WGS84Point

class ShareLinkUriOutputTest : GeoTest {
    private val geometries = mockGeometries()
    private val coordinateConverter = CoordinateConverter(geometries)
    private val uriQuote = FakeUriQuote

    @Test
    fun getText_whenPointIsWGS84AndWithinMainlandChinaAndLinkSrsIsGCJ02MainlandChina_returnsUriWithConvertedCoordinates() {
        val link = Link(
            coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
            nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
            srs = Srs.GCJ02_MAINLAND_CHINA,
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=31.2285067%2C121.475524",
            ShareLinkUriOutput(link, coordinateConverter)
                .getText(
                    WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                    uriQuote,
                ),
        )
    }

    @Test
    fun getText_whenPointIsGCJ02AndWithinMainlandChinaAndLinkSrsIsGCJ02MainlandChina_returnsUriWithUnchangedCoordinates() {
        val link = Link(
            coordsUriTemplate = "https://www.google.com/maps/search/?api=1&query={lat}%2C{lon}",
            nameUriTemplate = "https://www.google.com/maps/search/?api=1&query={q}",
            srs = Srs.GCJ02_MAINLAND_CHINA,
        )
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=31.2285069%2C121.4755246",
            ShareLinkUriOutput(link, coordinateConverter)
                .getText(
                    GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                    uriQuote,
                ),
        )
    }
}
