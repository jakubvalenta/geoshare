package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.GeoTest
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class OpenStreetViewGoogleUriOutputTest : GeoTest {
    private val geometries = mockGeometries()
    private val coordinateConverter = CoordinateConverter(geometries)
    private val uriQuote = FakeUriQuote

    @Test
    fun getText_whenPointIsWGS84AndWithinMainlandChinaAndPackageNameRequiresGCJ02MainlandChina_returnsUriWithConvertedCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285067,121.475524",
            OpenStreetViewGoogleUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                .getText(
                    WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                    uriQuote,
                ),
        )
    }

    @Test
    fun getText_whenPointIsGCJ02AndWithinMainlandChinaAndPackageNameRequiresGCJ02MainlandChina_returnsUriWithUnchangedCoordinates() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "google.streetview:cbll=31.2285069,121.4755246",
            OpenStreetViewGoogleUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                .getText(
                    GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED),
                    uriQuote,
                ),
        )
    }
}
