package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.GeoTest
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OpenDisplayCartesIGNUrlOutputTest : GeoTest {
    private val geometries = mockGeometries()
    private val coordinateConverter = CoordinateConverter(geometries)
    private val uriQuote = FakeUriQuote

    @Test
    fun getText_whenPointIsWGS84AndWithinMainlandChina_returnsUrlWithConvertedCoordinates() {
        assertEquals(
            "https://cartes-ign.ign.fr?lng=121.4709921&lat=31.2304417&z=3.14",
            OpenDisplayCartesIGNUrlOutput(PackageNames.TEST, coordinateConverter)
                .getText(
                    WGS84Point(31.23044166868017, 121.47099209401793, z = 3.14, source = Source.GENERATED),
                    uriQuote,
                ),
        )
    }

    @Test
    fun getText_whenPointIsGCJ02AndWithinMainlandChinaAndLinkSrsIsGCJ02MainlandChina_returnsUrlWithUnchangedCoordinates() {
        assertEquals(
            "https://cartes-ign.ign.fr?lng=121.4709921&lat=31.2304417&z=3.14",
            OpenDisplayCartesIGNUrlOutput(PackageNames.TEST, coordinateConverter)
                .getText(
                    GCJ02Point(31.22850685422705, 121.47552456472106, z = 3.14, source = Source.GENERATED),
                    uriQuote,
                ),
        )
    }

    @Test
    fun getText_whenPointDoesNotHaveCoordinates_returnsNull() {
        assertNull(
            OpenDisplayCartesIGNUrlOutput(PackageNames.TEST, coordinateConverter)
                .getText(
                    GCJ02Point(name = "foo bar", source = Source.GENERATED),
                    uriQuote,
                )
        )
    }
}
