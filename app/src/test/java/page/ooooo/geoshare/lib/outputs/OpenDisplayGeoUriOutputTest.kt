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

class OpenDisplayGeoUriOutputTest : GeoTest {
    private val geometries = mockGeometries()
    private val coordinateConverter = CoordinateConverter(geometries)
    private val uriQuote = FakeUriQuote

    @Test
    fun getText_returnsGeoUriWithCoordinatesConvertedToSrsBasedOnPackageName() {
        val point = WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED)
        // WGS-84
        assertEquals(
            "geo:31.2304417,121.4709921",
            OpenDisplayGeoUriOutput(PackageNames.TEST, coordinateConverter)
                .getText(point, uriQuote),
        )
        // GCJ-02
        assertEquals(
            "geo:31.2285067,121.475524?q=31.2285067,121.475524",
            OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                .getText(point, uriQuote),
        )
        // GCJ-02
        assertEquals(
            "geo:31.2285067,121.475524?q=31.2285067,121.475524",
            OpenDisplayGeoUriOutput(PackageNames.GMAPS_WV, coordinateConverter)
                .getText(point, uriQuote),
        )
        // GCJ-02
        assertEquals(
            "geo:31.2285067,121.475524?q=31.2285067,121.475524",
            OpenDisplayGeoUriOutput(PackageNames.AMAP, coordinateConverter)
                .getText(point, uriQuote),
        )
    }

    @Test
    fun getText_returnsGeoUriWithFlavorBasedOnPackageName() {
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
                OpenDisplayGeoUriOutput(packageName, coordinateConverter)
                    .getText(point, uriQuote),
            )
        }
    }

    @Test
    fun getText_whenPointIsGCJ02AndWithinMainlandChinaAndPackageNameRequiresWGS84_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:39.9191328,116.3254076?q=39.9191328,116.3254076",
            OpenDisplayGeoUriOutput(PackageNames.OSMAND_PLUS, coordinateConverter)
                .getText(
                    GCJ02Point(39.920439, 116.331538, source = Source.GENERATED),
                    uriQuote,
                )
        )
    }

    @Test
    fun getText_whenPointIsGCJ02AndWithinWesternJapanAndPackageNameRequiresGCJ02MainlandChina_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:34.5953404,133.7527361?q=34.5953404,133.7527361",
            OpenDisplayGeoUriOutput(PackageNames.GOOGLE_MAPS, coordinateConverter)
                .getText(
                    GCJ02Point(34.5945482, 133.7583428, source = Source.GENERATED),
                    uriQuote,
                )
        )
    }
}
