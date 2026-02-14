package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AmapAppType
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.BaiduMapAppType
import page.ooooo.geoshare.lib.android.DefaultGeoUriAppType
import page.ooooo.geoshare.lib.android.GMapsWVAppType
import page.ooooo.geoshare.lib.android.GarminAppType
import page.ooooo.geoshare.lib.android.GoogleMapsAppType
import page.ooooo.geoshare.lib.android.OeffiAppType
import page.ooooo.geoshare.lib.point.GCJ02Point
import page.ooooo.geoshare.lib.point.WGS84Point

class GeoUriOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = GeoUriOutput

    @Test
    fun copyAction_whenLastPointDoesNotHaveCoordinates_returnsUriWithZeroCoordinates() {
        assertEquals(
            "geo:0,0",
            output.getPointsActions().firstNotNullOf { it as? CopyAction }
                .getText(persistentListOf(), null, uriQuote),
        )
    }

    @Test
    fun copyAction_whenLastPointHasName_returnsUriWithZeroCoordinatesAndQParam() {
        assertEquals(
            "geo:0,0?q=foo%20bar",
            output.getPointsActions().firstNotNullOf { it as? CopyAction }
                .getText(persistentListOf(WGS84Point(name = "foo bar")), null, uriQuote),
        )
    }

    @Test
    fun copyAction_whenLastPointHasCoordinates_returnsUriWithPinParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            output.getPointsActions().firstNotNullOf { it as? CopyAction }
                .getText(persistentListOf(WGS84Point(50.123456, -11.123456)), null, uriQuote),
        )
    }

    @Test
    fun copyAction_whenLastPointHasCoordinatesAndName_returnsUriWithPinAndNameParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
            output.getPointsActions().firstNotNullOf { it as? CopyAction }
                .getText(persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar")), null, uriQuote),
        )
    }

    @Test
    fun copyOutput_whenLastPointHasCoordinatesAndNameAndZoom_returnsUriWithPinAndNameAndZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456(foo%20bar)",
            output
                .getPointsActions().firstNotNullOf { it as? CopyAction }
                .getText(
                    persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.4, name = "foo bar")),
                    null,
                    uriQuote
                ),
        )
    }

    @Test
    fun copyOutput_whenLastPointHasZoom_returnsUriWithZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?z=3.4&q=50.123456,-11.123456",
            persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.4)).let { points ->
                output
                    .getAppActions(listOf(App("com.example.test", DefaultGeoUriAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            },
        )
    }

    @Test
    fun copyOutput_whenLastPointHasZoomAndAppTypeDoesNotSupportZoom_returnsUriWithoutZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.4)).let { points ->
                output
                    .getAppActions(listOf(App("com.garmin.android.apps.explore", GarminAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            },
        )
    }

    @Test
    fun appAction_whenLastPointHasCoordinatesAndName_returnsUriWithPinAndNameParam() {
        assertEquals(
            listOf(
                "com.example.test" to "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo%20bar)",
                OeffiAppType.PACKAGE_NAME to "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar")).let { points ->
                output.getAppActions(
                    listOf(
                        App("com.example.test", DefaultGeoUriAppType),
                        App(OeffiAppType.PACKAGE_NAME, OeffiAppType),
                    )
                )
                    .filter { (_, action) -> action.isEnabled(points, null) }
                    .map { (packageName, action) ->
                        packageName to action.getUriString(points, null, uriQuote)
                    }
            },
        )
    }

    @Test
    fun appAction_whenLastPointHasCoordinatesAndNameAndAppTypeDoesNotSupportName_returnsUriWithPinAndWithoutNameParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=50.123456,-11.123456",
            persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar")).let { points ->
                output.getAppActions(listOf(App(OeffiAppType.PACKAGE_NAME, OeffiAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            },
        )
    }

    @Test
    fun appAction_whenLastPointHasCoordinatesAppTypeDoesNotSupportNameOrPin_returnsUriWithouPinParam() {
        assertEquals(
            "geo:50.123456,-11.123456",
            persistentListOf(WGS84Point(50.123456, -11.123456)).let { points ->
                output
                    .getAppActions(listOf(App(BaiduMapAppType.PACKAGE_NAME, BaiduMapAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            },
        )
    }

    @Test
    fun appAction_whenLastPointHasCoordinatesAndNameAndAppTypeDoesNotSupportNameOrPin_returnsUriWithoutNameOrPinParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=foo%20bar",
            persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar")).let { points ->
                output
                    .getAppActions(listOf(App(BaiduMapAppType.PACKAGE_NAME, BaiduMapAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            },
        )
    }

    @Test
    fun appAction_whenLastPointHasCoordinatesAndZoomAndNameAndAppTypeDoesNotSupportZoomAndQ_returnsUriWithoutZParam() {
        assertEquals(
            "geo:50.123456,-11.123456?q=foo%20bar",
            persistentListOf(WGS84Point(50.123456, -11.123456, z = 12.0, name = "foo bar")).let { points ->
                output
                    .getAppActions(listOf(App(BaiduMapAppType.PACKAGE_NAME, BaiduMapAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            },
        )
    }

    @Test
    fun appOutput_whenLastPointIsInGCJ02AndIsInJapan_returnsUriWithCoordinatesUnchanged() {
        assertEquals(
            "geo:34.5945482,133.7583428?q=34.5945482,133.7583428",
            persistentListOf(GCJ02Point(34.5945482, 133.7583428)).let { points ->
                output.getAppActions(listOf(App("com.example.test", DefaultGeoUriAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            }
        )
    }

    @Test
    fun appOutput_whenLastPointIsInGCJ02AndIsInChina_returnsUriWithCoordinatesConvertedToWGS84() {
        assertEquals(
            "geo:39.9191328,116.3254076?q=39.9191328,116.3254076",
            persistentListOf(GCJ02Point(39.920439, 116.331538)).let { points ->
                output.getAppActions(listOf(App("com.example.test", DefaultGeoUriAppType)))
                    .firstNotNullOf { (_, action) -> action.takeIf { it.isEnabled(points, null) } }
                    .getUriString(points, null, uriQuote)
            }
        )
    }

    @Test
    fun appOutput_whenLastPointIsInWGS84AndPackageNameRequiresGCJ02_returnsUriWithCoordinatesConvertedToGCJ02() {
        assertEquals(
            listOf(
                "com.example.test" to "geo:31.2304417,121.4709921?q=31.2304417,121.4709921", // WGS 84
                GoogleMapsAppType.PACKAGE_NAME to "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
                GMapsWVAppType.PACKAGE_NAME to "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
                AmapAppType.PACKAGE_NAME to "geo:31.2285067,121.475524?q=31.2285067,121.475524", // GCJ-02
            ),
            persistentListOf(WGS84Point(31.23044166868017, 121.47099209401793)).let { points ->
                output.getAppActions(
                    listOf(
                        App("com.example.test", DefaultGeoUriAppType), // WGS 84
                        App(GoogleMapsAppType.PACKAGE_NAME, GoogleMapsAppType), // GCJ-02
                        App(GMapsWVAppType.PACKAGE_NAME, GMapsWVAppType), // GCJ-02
                        App(AmapAppType.PACKAGE_NAME, AmapAppType), // GCJ-02
                    )
                )
                    .filter { (_, action) -> action.isEnabled(points, null) }
                    .map { (packageName, action) -> packageName to action.getUriString(points, null, uriQuote) }
            }
        )
    }
}
