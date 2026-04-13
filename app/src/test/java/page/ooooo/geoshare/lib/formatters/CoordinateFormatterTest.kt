package page.ooooo.geoshare.lib.formatters

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CoordinateFormatterTest {
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val coordinateFormatter = CoordinateFormatter(coordinateConverter)

    @Test
    fun formatDecCoords_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            "-17.2165721, -149.9470294",
            coordinateFormatter.formatDecCoords(WGS84Point(-17.2165721, -149.9470294, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDecCoords_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            "52.5067296, 13.2599309",
            coordinateFormatter.formatDecCoords(WGS84Point(52.5067296, 13.2599309, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDecCoords_returnsZerosForZeroCoordinates() {
        assertEquals(
            "0, 0",
            coordinateFormatter.formatDecCoords(WGS84Point(0.0, 0.0, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDecCoords_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            "0.5, 0.5",
            coordinateFormatter.formatDecCoords(WGS84Point(0.5, 0.5, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDecCoords_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            "-10, -20",
            coordinateFormatter.formatDecCoords(WGS84Point(-10.0, -20.0, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDecCoords_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            "-10.5, -20.5",
            coordinateFormatter.formatDecCoords(WGS84Point(-10.5, -20.5, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            "17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W",
            coordinateFormatter.formatDegMinSecCoords(WGS84Point(-17.2165721, -149.9470294, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            "52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E",
            coordinateFormatter.formatDegMinSecCoords(WGS84Point(52.5067296, 13.2599309, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZerosForZeroCoordinates() {
        assertEquals(
            "0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E",
            coordinateFormatter.formatDegMinSecCoords(WGS84Point(0.0, 0.0, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            "0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E",
            coordinateFormatter.formatDegMinSecCoords(WGS84Point(0.5, 0.5, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            "10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W",
            coordinateFormatter.formatDegMinSecCoords(WGS84Point(-10.0, -20.0, source = Source.GENERATED)),
        )
    }

    @Test
    fun formatDegMinSecCoords_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            "10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W",
            coordinateFormatter.formatDegMinSecCoords(WGS84Point(-10.5, -20.5, source = Source.GENERATED)),
        )
    }
}
