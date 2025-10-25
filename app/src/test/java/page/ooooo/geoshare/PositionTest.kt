package page.ooooo.geoshare

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position

class PositionTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun toParamsString_returnsQueryAndZoomButNotPoints() {
        assertEquals(
            "foo bar \u2022 z3.4",
            Position(
                q = "foo bar",
                z = "3.4",
                points = persistentListOf(
                    Point("59.1293656", "11.4585672"),
                    Point("59.4154007", "11.659710599999999"),
                    Point("59.147731699999994", "11.550661199999999"),
                ),
            ).toParamsString(" \u2022 "),
        )
    }

    @Test
    fun toDegMinSecCoordsString_returnsSouthWestForNegativeCoordinates() {
        assertEquals(
            "17°\u00a012′\u00a059.65956″\u00a0S, 149°\u00a056′\u00a049.30584″\u00a0W",
            Position("-17.2165721", "-149.9470294").toDegMinSecCoordsString(),
        )
    }

    @Test
    fun toDegMinSecCoordsString_returnsNorthEastForPositiveCoordinates() {
        assertEquals(
            "52°\u00a030′\u00a024.22656″\u00a0N, 13°\u00a015′\u00a035.75124″\u00a0E",
            Position("52.5067296", "13.2599309").toDegMinSecCoordsString(),
        )
    }

    @Test
    fun toDegMinSecCoordsString_returnsZerosForZeroCoordinates() {
        assertEquals(
            "0°\u00a00′\u00a00.0″\u00a0N, 0°\u00a00′\u00a00.0″\u00a0E",
            Position("0", "0").toDegMinSecCoordsString(),
        )
    }

    @Test
    fun toDegMinSecCoordsString_returnsZeroDegForZeroDegCoordinates() {
        assertEquals(
            "0°\u00a030′\u00a00.0″\u00a0N, 0°\u00a030′\u00a00.0″\u00a0E",
            Position("0.5", "0.5").toDegMinSecCoordsString(),
        )
    }

    @Test
    fun toDegMinSecCoordsString_returnsZeroMinForZeroMinCoordinates() {
        assertEquals(
            "10°\u00a00′\u00a00.0″\u00a0S, 20°\u00a00′\u00a00.0″\u00a0W",
            Position("-10", "-20").toDegMinSecCoordsString(),
        )
    }

    @Test
    fun toDegMinSecCoordsString_returnsZerosSecForZeroSecCoordinates() {
        assertEquals(
            "10°\u00a030′\u00a00.0″\u00a0S, 20°\u00a030′\u00a00.0″\u00a0W",
            Position("-10.5", "-20.5").toDegMinSecCoordsString(),
        )
    }

    @Test
    fun toGpx() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="50.123456" lon="-11.123456" />
<wpt lat="52.5067296" lon="13.2599309" />
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point("50.123456", "-11.123456"),
                        Point("52.5067296", "13.2599309"),
                    ),
                ).toGpx(this, uriQuote)
            }.toString()
        )
    }

    @Test
    fun toGpx_escapesSpecialCharacters() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="%22" lon="%3C" />
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point("\"", "<"),
                    ),
                ).toGpx(this, uriQuote)
            }.toString()
        )
    }

    @Test
    fun toGpx_noPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
</gpx>
""",
            StringBuilder().apply {
                Position().toGpx(this, uriQuote)
            }.toString()
        )
    }
}
