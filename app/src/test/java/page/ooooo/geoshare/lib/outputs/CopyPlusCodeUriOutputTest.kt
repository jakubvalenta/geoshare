package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.GeoTest
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CopyPlusCodeUriOutputTest : GeoTest {
    private val geometries = mockGeometries()
    private val coordinateConverter = CoordinateConverter(geometries)
    private val uriQuote = FakeUriQuote

    @Test
    fun getText_pointIsOutsideMainlandChina_returnsWGS84PlusCode() {
        assertEquals(
            "https://www.google.com/maps/place/9C2C4VFG%2B9JM",
            CopyPlusCodeUriOutput(coordinateConverter)
                .getText(WGS84Point(50.123456, -11.123456, source = Source.GENERATED), uriQuote),
        )
    }

    @Test
    fun getText_pointIsWithinMainlandChina_returnsGCJ02PlusCode() {
        assertEquals(
            "https://www.google.com/maps/place/8PFRW98W%2BWRG",
            CopyPlusCodeUriOutput(coordinateConverter)
                .getText(GCJ02Point(39.917313, 116.397063, source = Source.GENERATED), uriQuote),
        )
    }
}
