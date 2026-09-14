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

class OpenDisplayGeoUriOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val uriQuote = FakeUriQuote

    @Test
    fun getUriString_returnsGeoUriWithCoordinatesConvertedToSrsBasedOnPackageName() {
        val point = WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED)
        // WGS-84
        assertEquals(
            "geo:31.2304417,121.4709921",
            OpenDisplayGeoUriOutput(UriActivity(PackageNames.TEST, UriScheme.GEO), coordinateConverter)
                .getUriString(point, uriQuote),
        )
        // GCJ-02
        assertEquals(
            "geo:31.2285067,121.475524?q=31.2285067,121.475524",
            OpenDisplayGeoUriOutput(UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter)
                .getUriString(point, uriQuote),
        )
        // GCJ-02
        assertEquals(
            "geo:31.2285067,121.475524?q=31.2285067,121.475524",
            OpenDisplayGeoUriOutput(UriActivity(PackageNames.GMAPS_WV, UriScheme.GEO), coordinateConverter)
                .getUriString(point, uriQuote),
        )
        // GCJ-02
        assertEquals(
            "geo:31.2285067,121.475524?q=31.2285067,121.475524",
            OpenDisplayGeoUriOutput(UriActivity(PackageNames.AMAP, UriScheme.GEO), coordinateConverter)
                .getUriString(point, uriQuote),
        )
    }

    @Test
    fun getUriString_returnsGeoUriWithFlavorBasedOnPackageName() {
        val point = WGS84Point(
            31.23044166868017, 121.47099209401793,
            z = 3.4, name = "foo bar", source = Source.GENERATED,
        )
        mapOf(
            PackageNames.AMAP to "geo:31.2285067,121.475524?z=3.4&q=31.2285067,121.475524(foo%20bar)",
            PackageNames.BAIDU_MAP to "geo:31.2304417,121.4709921?z=3.4",
            PackageNames.COMAPS_FDROID to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.GARMIN_EXPLORE to "geo:31.2304417,121.4709921?q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.GMAPS_WV to "geo:31.2285067,121.475524?z=3.4&q=31.2285067,121.475524(foo%20bar)",
            PackageNames.GOOGLE_MAPS to "geo:31.2285067,121.475524?z=3.4&q=31.2285067,121.475524(foo%20bar)",
            PackageNames.HERE_WEGO to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.KOMOOT to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921",
            PackageNames.LOCUS_MAP to "geo:31.2304417,121.4709921?z=3.4&q=foo%20bar",
            PackageNames.MAGIC_EARTH to "geo:31.2304417,121.4709921",
            PackageNames.MAPS_ME to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.MAPY_COM to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.OEFFI to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921",
            PackageNames.ORGANIC_MAPS to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.OSMAND to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.OSMAND_PLUS to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.SYGIC to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
            PackageNames.TEST to "geo:31.2304417,121.4709921",
            PackageNames.TOMTOM to "geo:31.2304417,121.4709921",
            PackageNames.VESPUCCI to "geo:31.2304417,121.4709921?z=3.4&q=31.2304417,121.4709921(foo%20bar)",
        ).forEach { (packageName, expectedGeoUriString) ->
            assertEquals(
                expectedGeoUriString,
                OpenDisplayGeoUriOutput(UriActivity(packageName, UriScheme.GEO), coordinateConverter)
                    .getUriString(point, uriQuote),
            )
        }
    }

    @Test
    fun getUriString_whenPointIsGCJ02AndWithinMainlandChinaAndPackageNameRequiresWGS84_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:39.9191328,116.3254076?q=39.9191328,116.3254076",
            OpenDisplayGeoUriOutput(UriActivity(PackageNames.OSMAND_PLUS, UriScheme.GEO), coordinateConverter)
                .getUriString(
                    GCJ02Point(39.920439, 116.331538, source = Source.GENERATED),
                    uriQuote,
                )
        )
    }

    @Test
    fun getUriString_whenPointIsGCJ02AndWithinWesternJapanAndPackageNameRequiresGCJ02MainlandChina_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:34.5953404,133.7527361?q=34.5953404,133.7527361",
            OpenDisplayGeoUriOutput(UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO), coordinateConverter)
                .getUriString(
                    GCJ02Point(34.5945482, 133.7583428, source = Source.GENERATED),
                    uriQuote,
                )
        )
    }
}
