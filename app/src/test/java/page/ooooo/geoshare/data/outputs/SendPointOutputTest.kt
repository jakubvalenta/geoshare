package page.ooooo.geoshare.data.outputs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.data.android.MimeType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.TextActivity
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.GCJ02Point
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.geo.mockGeometries
import page.ooooo.geoshare.data.uri.FakeUriQuote

class SendPointOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val uriQuote = FakeUriQuote
    private val activity = TextActivity(PackageNames.CONVERSATIONS, MimeType.TEXT_PLAIN)
    private val output = SendPointOutput(activity, coordinateConverter, uriQuote)

    @Test
    fun getUriString_whenPointIsWGS84AndWithinMainlandChina_returnsUriWithConvertedCoordinates() {
        assertEquals(
            "https://maps.google.com/?q=31.2285067%2C121.475524",
            output.getUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun getUriString_whenPointIsGCJ02AndWithinMainlandChinaAndLinkSrsIsGCJ02MainlandChina_returnsUriWithUnchangedCoordinates() {
        assertEquals(
            "https://maps.google.com/?q=31.2285069%2C121.4755246",
            output.getUriString(
                GCJ02Point(31.22850685422705, 121.47552456472106, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun getUriString_whenPointDoesNotHaveCoordinates_returnsUriWithSearchQuery() {
        assertEquals(
            "https://maps.google.com/?q=foo%20bar",
            output.getUriString(
                GCJ02Point(name = "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun isEnabled_whenPointDoesNotHaveCoordinates_returnsTrue() {
        assertTrue(output.isEnabled(WGS84Point(name = "foo bar", source = Source.GENERATED)))
    }

    @Test
    fun isEnabled_whenPointHasNeitherCoordinatesNorName_returnsFalse() {
        val point = WGS84Point(source = Source.GENERATED)
        assertNull(output.getUriString(point))
        assertFalse(output.isEnabled(point))
    }

    private fun outputFor(packageName: String = PackageNames.MY_BMW_ROW) =
        SendPointOutput(TextActivity(packageName, MimeType.TEXT_PLAIN), coordinateConverter, uriQuote)

    @Test
    fun getUriString_whenActivityIsCarApp_returnsPlaceUriWithUnencodedComma() {
        assertEquals(
            "https://www.google.com/maps/place/48.1374,11.5755",
            outputFor().getUriString(WGS84Point(48.1374, 11.5755, source = Source.GENERATED)),
        )
    }

    @Test
    fun getUriString_whenActivityIsCarAppAndCoordinatesAreNegative_returnsPlaceUriWithNegativeCoordinates() {
        assertEquals(
            "https://www.google.com/maps/place/-33.8568,-151.2153",
            outputFor().getUriString(WGS84Point(-33.8568, -151.2153, source = Source.GENERATED)),
        )
    }

    @Test
    fun getUriString_whenActivityIsCarAppAndPointHasCoordinatesAndName_ignoresName() {
        assertEquals(
            "https://www.google.com/maps/place/48.1374,11.5755",
            outputFor().getUriString(
                WGS84Point(48.1374, 11.5755, name = "Marienplatz", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun getUriString_whenActivityIsCarAppAndPointIsWGS84AndWithinMainlandChina_returnsUriWithConvertedCoordinates() {
        assertEquals(
            "https://www.google.com/maps/place/31.2285067,121.475524",
            outputFor().getUriString(
                WGS84Point(31.23044166868017, 121.47099209401793, source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun getUriString_whenActivityIsCarAppAndPointDoesNotHaveCoordinates_returnsNullAndIsDisabled() {
        val point = WGS84Point(name = "Marienplatz 1, München", source = Source.GENERATED)
        assertNull(outputFor().getUriString(point))
        assertFalse(outputFor().isEnabled(point))
    }

    @Test
    fun getUriString_whenActivityIsAnyCarApp_returnsPlaceUri() {
        for (packageName in listOf(
            PackageNames.MINI_NA,
            PackageNames.MINI_ROW,
            PackageNames.MY_BMW_NA,
            PackageNames.MY_BMW_ROW,
        )) {
            assertEquals(
                packageName,
                "https://www.google.com/maps/place/1.5,2.5",
                outputFor(packageName).getUriString(WGS84Point(1.5, 2.5, source = Source.GENERATED)),
            )
        }
    }

    @Test
    fun getUriString_whenActivityIsMessagingApp_keepsEncodedComma() {
        for (packageName in listOf(PackageNames.SIGNAL, PackageNames.TELEGRAM, PackageNames.WHATSAPP)) {
            assertEquals(
                packageName,
                "https://maps.google.com/?q=1.5%2C2.5",
                outputFor(packageName).getUriString(WGS84Point(1.5, 2.5, source = Source.GENERATED)),
            )
        }
    }
}
