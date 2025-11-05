package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

class CoordinatesOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = CoordinatesOutputGroup

    @Test
    fun supportingTextOutput_returnsQueryAndZoomButNotPoints() {
        assertEquals(
            "foo bar\t\tz3.4",
            outputGroup.getSupportingTextOutput().getText(
                Position(
                    q = "foo bar",
                    z = "3.4",
                    points = persistentListOf(
                        Point("59.1293656", "11.4585672"),
                        Point("59.4154007", "11.659710599999999"),
                        Point("59.147731699999994", "11.550661199999999"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun copyOutput_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            Action.Copy("17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W"),
            outputGroup.getActionOutputs().first().getAction(Position("-17.2165721", "-149.9470294"), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            Action.Copy("52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E"),
            outputGroup.getActionOutputs().first().getAction(Position("52.5067296", "13.2599309"), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsZerosForZeroCoordinates() {
        assertEquals(
            Action.Copy("0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E"),
            outputGroup.getActionOutputs().first().getAction(Position("0", "0"), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            Action.Copy("0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E"),
            outputGroup.getActionOutputs().first().getAction(Position("0.5", "0.5"), uriQuote),
        )
    }

    @Test
    fun copyOutput_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            Action.Copy("10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W"),
            outputGroup.getActionOutputs().first().getAction(Position("-10", "-20"), uriQuote)
        )
    }

    @Test
    fun copyOutput_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            Action.Copy("10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W"),
            outputGroup.getActionOutputs().first().getAction(Position("-10.5", "-20.5"), uriQuote)
        )
    }

}
