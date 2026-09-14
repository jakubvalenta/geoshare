package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
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

class OpenNavigationGoogleUriOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val uriQuote = FakeUriQuote
    private val activity = UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION)
    private val output = OpenNavigationGoogleUriOutput(activity, coordinateConverter)

    @Test
    fun getUriString_whenPointIsWGS84AndWithinMainlandChinaAndPackageNameRequiresGCJ02MainlandChina_returnsUriWithConvertedCoordinates() {
        assertEquals(
            "google.navigation:q=31.2285067,121.475524",
            output.getUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }

    @Test
    fun getUriString_whenPointIsGCJ02AndWithinMainlandChinaAndPackageNameRequiresGCJ02MainlandChina_returnsUriWithUnchangedCoordinates() {
        assertEquals(
            "google.navigation:q=31.2285069,121.4755246",
            output.getUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                uriQuote,
            ),
        )
    }
}
