package page.ooooo.geoshare.lib.formatters

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.BaseGeometriesTest
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class GeoUriFormatterTest : BaseGeometriesTest() {
    private val coordinateConverter = CoordinateConverter(geometries)
    private val geoUriFormatter = GeoUriFormatter(coordinateConverter)
    private val uriQuote: UriQuote = FakeUriQuote

    @Test
    fun formatGeoUriString_whenLastPointDoesNotHaveCoordinates_returnsUriWithZeroCoordinates() {
        assertEquals(
            "geo:0,0",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasName_returnsUriWithZeroCoordinatesAndQParam() {
        assertEquals(
            "geo:0,0?q=foo%20bar",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(name = "foo bar", source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinates_returnsUriWithPin() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndZoom_returnsUriWithPinAndZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndName_returnsUriWithPinWithName() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndZoom_returnsUriWithPinWithNameAndZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456(foo%20bar)",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, name = "foo bar", source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndPinFlavorIsCoordsOnlyInQ_returnsUriWithPinWithoutName() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                GeoUriFormatter.Flavor(
                    pin = GeoUriFormatter.Flavor.PinFlavor.COORDS_ONLY_IN_Q,
                    zoom = GeoUriFormatter.Flavor.ZoomFlavor.ANY,
                ),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndPinFlavorIsNotAvailable_returnsUriWithoutPinAndWithoutQParam() {
        assertEquals(
            "geo:50.123456,-11.123456",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, name = "foo bar", source = Source.GENERATED),
                GeoUriFormatter.Flavor(
                    pin = GeoUriFormatter.Flavor.PinFlavor.NOT_AVAILABLE,
                    zoom = GeoUriFormatter.Flavor.ZoomFlavor.ANY,
                ),
                uriQuote,
            )
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndNameAndZoomAndZoomFlavorIsAloneOnly_returnsUriWithoutZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 12.0, name = "foo bar", source = Source.GENERATED),
                GeoUriFormatter.Flavor(
                    pin = GeoUriFormatter.Flavor.PinFlavor.COORDS_AND_NAME_IN_Q,
                    zoom = GeoUriFormatter.Flavor.ZoomFlavor.ALONE_ONLY,
                ),
                uriQuote,
            )
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointHasCoordinatesAndZoomAndZoomFlavorIsNotAvailable_returnsUriWithoutZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            geoUriFormatter.formatGeoUriString(
                WGS84Point(50.123456, -11.123456, z = 3.4, source = Source.GENERATED),
                GeoUriFormatter.Flavor(
                    pin = GeoUriFormatter.Flavor.PinFlavor.COORDS_AND_NAME_IN_Q,
                    zoom = GeoUriFormatter.Flavor.ZoomFlavor.NOT_AVAILABLE,
                ),
                uriQuote,
            ),
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointIsInGCJ02AndIsInWesternJapan_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:34.5953404,133.7527361?q=34.5953404,133.7527361",
            geoUriFormatter.formatGeoUriString(
                GCJ02Point(34.5945482, 133.7583428, source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            )
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointIsInGCJ02AndIsWithinMainlandChina_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:39.9191328,116.3254076?q=39.9191328,116.3254076",
            geoUriFormatter.formatGeoUriString(
                GCJ02Point(39.920439, 116.331538, source = Source.GENERATED),
                GeoUriFormatter.Flavor.Best,
                uriQuote,
            )
        )
    }

    @Test
    fun formatGeoUriString_whenLastPointIsInWGS84AndPackageNameRequiresGCJ02_returnsUriWithCoordinatesConvertedToGCJ02() {
        assertEquals(
            listOf(
                "geo:31.2304417,121.4709921", // WGS 84
                "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
                "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
                "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
            ),
            listOf(
                PackageNames.TEST, // WGS 84
                PackageNames.GOOGLE_MAPS, // GCJ-02
                PackageNames.GMAPS_WV, // GCJ-02
                PackageNames.AMAP, // GCJ-02
            ).map { packageName ->
                geoUriFormatter.formatGeoUriString(
                    WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED),
                    packageName,
                    uriQuote,
                )
            }
        )
    }

    @Test
    fun formatGeoUriString_choosesCorrectFlavorBasedOnPackageName() {
        val point =
            WGS84Point(31.23044166868017, 121.47099209401793, z = 3.4, name = "foo bar", source = Source.GENERATED)
        val expectedUriStrings = mapOf(
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
        )
        assertEquals(
            expectedUriStrings,
            expectedUriStrings.mapValues { (packageName) ->
                geoUriFormatter.formatGeoUriString(point, packageName, uriQuote)
            },
        )
    }
}
