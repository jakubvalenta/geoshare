package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class MagicEarthOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = MagicEarthOutputGroup

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndZoom_returnsUriWithCoordinatesAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Action.Copy("magicearth://?lat=50.123456&lon=-11.123456&zoom=3.4"),
            outputGroup.getActionOutputs().first()
                .getAction(Position("50.123456", "-11.123456", z = "3.4"), uriQuote),
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndQueryAndZoom_returnsUriWithCoordinatesAndQueryAndZoom() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Action.Copy("magicearth://?lat=50.123456&lon=-11.123456&q=foo%20bar&zoom=3.4"),
            outputGroup.getActionOutputs().first()
                .getAction(Position("50.123456", "-11.123456", q = "foo bar", z = "3.4"), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsDriveToAndDriveViaUriStrings() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            listOf(
                Action.Copy("magicearth://?drive_to&lat=50.123456&lon=-11.123456"),
                Action.Copy("magicearth://?drive_via&lat=50.123456&lon=-11.123456"),
            ),
            outputGroup.getActionOutputs().slice(1..2).map {
                it.getAction(Position("50.123456", "-11.123456"), uriQuote)
            },
        )
    }
}
