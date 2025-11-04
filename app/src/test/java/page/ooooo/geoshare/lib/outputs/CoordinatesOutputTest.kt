package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class CoordinatesOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val packageNames: List<String> = emptyList()

    @Test
    fun getSupportingText_returnsQueryAndZoomButNotPoints() {
        assertEquals(
            "foo bar\t\tz3.4",
            CoordinatesOutputManager.getOutputs(
                Position(
                    q = "foo bar",
                    z = "3.4",
                    points = persistentListOf(
                        Point("59.1293656", "11.4585672"),
                        Point("59.4154007", "11.659710599999999"),
                        Point("59.147731699999994", "11.550661199999999"),
                    ),
                ),
                packageNames,
                uriQuote,
            ).getSupportingText(),
        )
    }

    @Test
    fun getOutputs_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            Action.Copy("17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W"),
            CoordinatesOutputManager.getOutputs(Position("-17.2165721", "-149.9470294"), packageNames, uriQuote)
                .firstNotNullOfOrNull { (it as? Output.Action)?.action }
        )
    }

    @Test
    fun getOutputs_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            Action.Copy("52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E"),
            CoordinatesOutputManager.getOutputs(Position("52.5067296", "13.2599309"), packageNames, uriQuote)
                .firstNotNullOfOrNull { (it as? Output.Action)?.action }
        )
    }

    @Test
    fun getOutputs_returnsZerosForZeroCoordinates() {
        assertEquals(
            Action.Copy("0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E"),
            CoordinatesOutputManager.getOutputs(Position("0", "0"), packageNames, uriQuote)
                .getActions().first().action,
        )
    }

    @Test
    fun getOutputs_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            Action.Copy("0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E"),
            CoordinatesOutputManager.getOutputs(Position("0.5", "0.5"), packageNames, uriQuote)
                .firstNotNullOfOrNull { (it as? Output.Action)?.action }
        )
    }

    @Test
    fun getOutputs_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            Action.Copy("10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W"),
            CoordinatesOutputManager.getOutputs(Position("-10", "-20"), packageNames, uriQuote)
                .getActions().first().action,
        )
    }

    @Test
    fun getOutputs_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            Action.Copy("10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W"),
            CoordinatesOutputManager.getOutputs(Position("-10.5", "-20.5"), packageNames, uriQuote)
                .getActions().first().action,
        )
    }

}
