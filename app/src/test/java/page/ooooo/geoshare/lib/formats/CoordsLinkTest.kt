package page.ooooo.geoshare.lib.formats

import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class CoordsLinkTest {
    @Test
    fun formatDecCoords_returnsSouthWestForNegativeCoordinates() {
        Assert.assertEquals(
            "-17.2165721, -149.9470294",
            CoordsFormat.formatDecCoords(WGS84Point(-17.2165721, -149.9470294)),
        )
    }

    @Test
    fun formatDecCoords_returnsNorthEastForPositiveCoordinates() {
        Assert.assertEquals(
            "52.5067296, 13.2599309",
            CoordsFormat.formatDecCoords(WGS84Point(52.5067296, 13.2599309)),
        )
    }

    @Test
    fun formatDecCoords_returnsZerosForZeroCoordinates() {
        Assert.assertEquals(
            "0, 0",
            CoordsFormat.formatDecCoords(WGS84Point(0.0, 0.0)),
        )
    }

    @Test
    fun formatDecCoords_returnsZeroDegForZeroDegCoordinates() {
        Assert.assertEquals(
            "0.5, 0.5",
            CoordsFormat.formatDecCoords(WGS84Point(0.5, 0.5)),
        )
    }

    @Test
    fun formatDecCoords_returnsZeroMinForZeroMinCoordinates() {
        Assert.assertEquals(
            "-10, -20",
            CoordsFormat.formatDecCoords(WGS84Point(-10.0, -20.0)),
        )
    }

    @Test
    fun formatDecCoords_returnsZerosSecForZeroSecCoordinates() {
        Assert.assertEquals(
            "-10.5, -20.5",
            CoordsFormat.formatDecCoords(WGS84Point(-10.5, -20.5)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsSouthWestForNegativeCoordinates() {
        Assert.assertEquals(
            "17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W",
            CoordsFormat.formatDegMinSecCoords(WGS84Point(-17.2165721, -149.9470294)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsNorthEastForPositiveCoordinates() {
        Assert.assertEquals(
            "52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E",
            CoordsFormat.formatDegMinSecCoords(WGS84Point(52.5067296, 13.2599309)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZerosForZeroCoordinates() {
        Assert.assertEquals(
            "0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E",
            CoordsFormat.formatDegMinSecCoords(WGS84Point(0.0, 0.0)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZeroDegForZeroDegCoordinates() {
        Assert.assertEquals(
            "0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E",
            CoordsFormat.formatDegMinSecCoords(WGS84Point(0.5, 0.5)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZeroMinForZeroMinCoordinates() {
        Assert.assertEquals(
            "10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W",
            CoordsFormat.formatDegMinSecCoords(WGS84Point(-10.0, -20.0)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZerosSecForZeroSecCoordinates() {
        Assert.assertEquals(
            "10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W",
            CoordsFormat.formatDegMinSecCoords(WGS84Point(-10.5, -20.5)),
        )
    }
}
