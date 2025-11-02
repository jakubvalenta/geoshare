package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class MagicEarthOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()

    @Test
    fun getMainUriString_whenPositionHasCoordinatesAndZoom_returnsUriWithCoordinatesAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "magicearth://?lat=50.123456&lon=-11.123456&zoom=3.4",
            MagicEarthOutput.getMainUriString(Position("50.123456", "-11.123456", z = "3.4"), uriQuote),
        )
    }

    @Test
    fun getMainUriString_whenPositionHasCoordinatesAndQueryAndZoom_returnsUriWithCoordinatesAndQueryAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            "magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4",
            MagicEarthOutput.getMainUriString(
                Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"), uriQuote
            ),
        )
    }

    @Test
    fun getExtraUriStrings_returnsDriveToAndDriveViaUriStrings() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            listOf(
                "magicearth://?drive_to=1&lat=50.123456&lon=-11.123456",
                "magicearth://?drive_via=1&lat=50.123456&lon=-11.123456",
            ),
            MagicEarthOutput.getExtraUriStrings(Point("50.123456", "-11.123456"), uriQuote),
        )
    }
}
