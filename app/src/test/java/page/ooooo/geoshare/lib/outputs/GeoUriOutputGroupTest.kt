package page.ooooo.geoshare.lib.outputs

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class GeoUriOutputGroupTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = GeoUriOutputGroup

    @Test
    fun copyOutput_whenPositionDoesNotHaveCoordinates_returnsUriWithZeroCoordinatesInPath() {
        assertEquals(
            listOf(
                Action.Copy("geo:0,0"),
                Action.OpenChooser("geo:0,0"),
            ),
            Position(Srs.WGS84).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinates_returnsUriWithCoordinatesInTheQParam() {
        assertEquals(
            listOf(
                Action.Copy("geo:50.123456,-11.123456?q=50.123456,-11.123456"),
                Action.OpenChooser("geo:50.123456,-11.123456?q=50.123456,-11.123456"),
            ),
            Position(Srs.WGS84, 50.123456, -11.123456).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndName_returnsUriWithCoordinatesAndNameInTheQParam() {
        assertEquals(
            listOf(
                Action.Copy("geo:50.123456,-11.123456?q=50.123456,-11.123456(foo+bar)"),
                Action.OpenChooser("geo:50.123456,-11.123456?q=50.123456,-11.123456(foo+bar)"),
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, name = "foo bar").let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun copyOutput_whenPositionHasCoordinatesAndQueryAndZoom_returnsUriWithTheQAndZParams() {
        assertEquals(
            listOf(
                Action.Copy("geo:50.123456,-11.123456?q=foo+bar&z=3.4"),
                Action.OpenChooser("geo:50.123456,-11.123456?q=foo+bar&z=3.4"),
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, q = "foo bar", z = 3.4).let { position ->
                outputGroup.getActionOutputs()
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }

    @Test
    fun appOutput_whenPositionHasCoordinatesAndName_returnsUriWithCoordinatesAndNameUnlessThePackageNameHasNameDisabled() {
        assertEquals(
            listOf(
                Action.OpenApp("com.example.test", "geo:50.123456,-11.123456?q=50.123456,-11.123456(foo+bar)"),
                @Suppress("SpellCheckingInspection")
                Action.OpenApp("de.schildbach.oeffi", "geo:50.123456,-11.123456?q=50.123456,-11.123456"),
            ),
            Position(Srs.WGS84, 50.123456, -11.123456, name = "foo bar").let { position ->
                outputGroup.getAppOutputs(
                    listOf(
                        "com.example.test",
                        @Suppress("SpellCheckingInspection")
                        "de.schildbach.oeffi",
                    )
                )
                    .filter { it.isEnabled(position) }
                    .map { it.getAction(position, uriQuote) }
            },
        )
    }
}
