package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.mockGeometries

class OpenDisplayCartesIGNUrlOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val uriQuote = FakeUriQuote
    private val activity = UriActivity(PackageNames.CARTES_IGN, UriScheme.CARTES_IGN)
    private val output = OpenDisplayCartesIGNUrlOutput(activity, coordinateConverter)

    @Test
    fun getUriString_whenPointIsWGS84AndWithinMainlandChina_returnsUrlWithConvertedCoordinates() {
        assertEquals(
            "https://cartes-ign.ign.fr?lng=121.4709921&lat=31.2304417&z=3.14",
            output.getUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, z = 3.14, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun getUriString_whenPointIsGCJ02AndWithinMainlandChinaAndLinkSrsIsGCJ02MainlandChina_returnsUrlWithUnchangedCoordinates() {
        assertEquals(
            "https://cartes-ign.ign.fr?lng=121.4709921&lat=31.2304417&z=3.14",
            output.getUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, z = 3.14, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun getUriString_whenPointDoesNotHaveCoordinates_returnsNull() {
        assertNull(
            output.getUriString(
                GCJ02Point(name = "foo bar", source = Source.GENERATED),
                uriQuote,
            )
        )
    }
}
