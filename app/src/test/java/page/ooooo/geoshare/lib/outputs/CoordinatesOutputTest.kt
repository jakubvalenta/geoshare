package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.WGS84Point

class CoordinatesOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = CoordinatesOutput

    @Test
    fun copyAction_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            listOf(
                "-17.2165721, -149.9470294",
                "17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W",
            ),
            persistentListOf(WGS84Point(-17.2165721, -149.9470294)).let { points ->
                output.getPositionActions().map { (it as? CopyAction)?.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            listOf(
                "52.5067296, 13.2599309",
                "52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E",
            ),
            persistentListOf(WGS84Point(52.5067296, 13.2599309)).let { points ->
                output.getPositionActions().map { (it as? CopyAction)?.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_returnsZerosForZeroCoordinates() {
        assertEquals(
            listOf(
                "0, 0",
                "0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E",
            ),
            persistentListOf(WGS84Point(0.0, 0.0)).let { points ->
                output.getPositionActions().map { (it as? CopyAction)?.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            listOf(
                "0.5, 0.5",
                "0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E",
            ),
            persistentListOf(WGS84Point(0.5, 0.5)).let { points ->
                output.getPositionActions().map { (it as? CopyAction)?.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            listOf(
                "-10, -20",
                "10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W",
            ),
            persistentListOf(WGS84Point(-10.0, -20.0)).let { points ->
                output.getPositionActions().map { (it as? CopyAction)?.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            listOf(
                "-10.5, -20.5",
                "10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W",
            ),
            persistentListOf(WGS84Point(-10.5, -20.5)).let { points ->
                output.getPositionActions().map { (it as? CopyAction)?.getText(points, null, uriQuote) }
            },
        )
    }

}
