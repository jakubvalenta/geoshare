package page.ooooo.geoshare.lib.position

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionTest {

    @Test
    fun setLastPointName_pointsAreNull_returnsPositionUnchanged() {
        val position = Position()
        assertEquals(
            position,
            position.setLastPointName("foo"),
        )
    }

    @Test
    fun setLastPointName_pointsAreEmpty_returnsPositionUnchanged() {
        val position = Position(points = persistentListOf())
        assertEquals(
            position,
            position.setLastPointName("foo"),
        )
    }

    @Test
    fun setLastPointName_pointsSizeIsOneAndLastPointHasName_returnsPositionUnchanged() {
        val position = Position(points = persistentListOf(Point(Srs.WGS84, 1.0, 2.0, "bar")))
        assertEquals(
            position,
            position.setLastPointName("foo"),
        )
    }

    @Test
    fun setMainPointName_pointsSizeIsOneAndLastPointDoesNotHaveName_returnsNewPositionWithLastPointNameSet() {
        val position = Position(points = persistentListOf(Point(Srs.WGS84, 1.0, 2.0)))
        assertEquals(
            Position(points = persistentListOf(Point(Srs.WGS84, 1.0, 2.0, "foo"))),
            position.setLastPointName("foo"),
        )
    }

    @Test
    fun setMainPointName_pointsSizeIsThreeAndLastPointDoesNotHaveName_returnsNewPositionWithLastPointNameSet() {
        val position = Position(
            points = persistentListOf(
                Point(Srs.WGS84, 1.0, 2.0),
                Point(Srs.WGS84, 3.0, 4.0),
                Point(Srs.WGS84, 5.0, 6.0),
            )
        )
        assertEquals(
            Position(
                points = persistentListOf(
                    Point(Srs.WGS84, 1.0, 2.0),
                    Point(Srs.WGS84, 3.0, 4.0),
                    Point(Srs.WGS84, 5.0, 6.0, "foo"),
                )
            ),
            position.setLastPointName("foo"),
        )
    }

    @Test
    fun writeGpxPoints() {
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
                        Point(Srs.WGS84, 50.123456, -11.123456),
                        Point(Srs.WGS84, 52.5067296, 13.2599309),
                    ),
                ).writeGpxPoints(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxPoints_escapesName() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="50.123456" lon="-11.123456">
    <name>&lt;script&gt;alert()&lt;/script&gt;</name>
</wpt>
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point(Srs.WGS84, 50.123456, -11.123456, name = "<script>alert()</script>"),
                    ),
                ).writeGpxPoints(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxPoints_noPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
</gpx>
""",
            StringBuilder().apply {
                Position().writeGpxPoints(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxRoute() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<rte>
<rtept lat="50.123456" lon="-11.123456" />
<rtept lat="52.5067296" lon="13.2599309" />
<rtept lat="53" lon="14" />
</rte>
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point(Srs.WGS84, 50.123456, -11.123456),
                        Point(Srs.WGS84, 52.5067296, 13.2599309),
                        Point(Srs.WGS84, 53.0, 14.0),
                    ),
                ).writeGpxRoute(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxRoute_escapesName() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<rte>
<rtept lat="50.123456" lon="-11.123456">
    <name>&lt;script&gt;alert()&lt;/script&gt;</name>
</rtept>
</rte>
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point(Srs.WGS84, 50.123456, -11.123456, name = "<script>alert()</script>"),
                    ),
                ).writeGpxRoute(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxRoute_noPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<rte>
</rte>
</gpx>
""",
            StringBuilder().apply {
                Position().writeGpxRoute(this)
            }.toString(),
        )
    }

    // 3520653,3317203 -> 28.696786436412197,121.45032959369263
    // 13502918.375,3315902.2199999997 -> 28.686779688493015,121.29095727245614
    // 12613508.26,2629182.88 -> 23.110319308993134,113.30138024838311,
    // 12611885.88,2631139.59 -> 23.12640943920111,113.28685011778114
    // 12613508.26,2629184.09 -> 23.11032936518539,113.30138025720055
    // 12614727.164999995,2631131.0213408465 -> 23.126697648344315,113.31230015743331
    // 12613424.449999997,2633524.681382545 -> 23.146380831856163,113.30063234845544
}
