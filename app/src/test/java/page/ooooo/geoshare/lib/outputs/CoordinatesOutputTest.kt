package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

class CoordinatesOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val outputGroup = CoordinatesOutput

    @Test
    fun descriptionOutput_returnsQueryAndZoomButNotPoints() {
        assertEquals(
            "foo bar\t\tz3.4",
            outputGroup.getDescription(
                Position(
                    q = "foo bar",
                    z = 3.4,
                    points = persistentListOf(
                        Point(Srs.WGS84, 59.1293656, 11.4585672),
                        Point(Srs.WGS84, 59.4154007, 11.659710599999999),
                        Point(Srs.WGS84, 59.147731699999994, 11.550661199999999),
                    ),
                ),
                uriQuote,
            ),
        )
    }

    @Test
    fun copyAction_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            "17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W",
            outputGroup.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, -17.2165721, -149.9470294), null, uriQuote),
        )
    }

    @Test
    fun copyAction_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            "52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E",
            outputGroup.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 52.5067296, 13.2599309), null, uriQuote),
        )
    }

    @Test
    fun copyAction_returnsZerosForZeroCoordinates() {
        assertEquals(
            "0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E",
            outputGroup.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 0.0, 0.0), null, uriQuote),
        )
    }

    @Test
    fun copyAction_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            "0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E",
            outputGroup.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, 0.5, 0.5), null, uriQuote),
        )
    }

    @Test
    fun copyAction_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            "10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W",
            outputGroup.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, -10.0, -20.0), null, uriQuote)
        )
    }

    @Test
    fun copyAction_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            "10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W",
            outputGroup.getPositionActions().firstNotNullOf { it as? CopyAction }
                .getText(Position(Srs.WGS84, -10.5, -20.5), null, uriQuote)
        )
    }

}
