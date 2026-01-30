package page.ooooo.geoshare.lib.point

import org.junit.Assert.assertEquals
import org.junit.Test

class PointTest {

    @Test
    fun wgs84Point_toGCJ02_emptyPoint_doesNotConvertCoordinates() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar"),
            WGS84Point(z = 3.14, name = "foo bar").toGCJ02(),
        )
    }

    @Test
    fun wgs84Point_toGCJ02_inChina_convertsCoordinates() {
        assertEquals(
            GCJ02Point(31.22281206362763, lon = 121.46840659541449, 3.14, "foo bar"),
            WGS84Point(31.224731304675522, lon = 121.46385323166844, 3.14, "foo bar").toGCJ02(),
        )
    }

    @Test
    fun wgs84Point_toGCJ02_notInChina_doesNotConvertCoordinates() {
        assertEquals(
            GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar"),
            WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar").toGCJ02(),
        )
    }

    @Test
    fun gcj02Point_toWGS84_emptyPoint_doesNotConvertCoordinates() {
        assertEquals(
            WGS84Point(z = 3.14, name = "foo bar"),
            GCJ02Point(z = 3.14, name = "foo bar").toWGS84(),
        )
    }

    @Test
    fun gcj02Point_toWGS84_inChina_convertsCoordinates() {
        assertEquals(
            WGS84Point(31.224731304675522, lon = 121.46385323166844, 3.14, "foo bar"),
            GCJ02Point(31.222811749011463, 121.46840706467624, 3.14, "foo bar").toWGS84(),
        )
    }

    @Test
    fun gcj02Point_toWGS84_notInChina_doesNotConvertCoordinates() {
        assertEquals(
            WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar"),
            GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar").toWGS84(),
        )
    }

    @Test
    fun bd09MCPoint_toGCJ02_emptyPoint_doesNotConvertCoordinates() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar"),
            BD09MCPoint(z = 3.14, name = "foo bar").toGCJ02()
        )
    }

    @Test
    fun bd09MCPoint_toGCJ02_convertsCoordinates() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar"),
            BD09MCPoint(z = 3.14, name = "foo bar").toGCJ02()
        )
        assertEquals(
            GCJ02Point(28.696786436412197, 121.45032959369264, 3.14, "foo bar"),
            BD09MCPoint(3317203.0, 13520653.0, 3.14, "foo bar").toGCJ02()
        )
        assertEquals(
            GCJ02Point(28.686779688493015, 121.29095727245614, 3.14, "foo bar"),
            BD09MCPoint(3315902.2199999997, 13502918.375, 3.14, "foo bar").toGCJ02()
        )
        assertEquals(
            GCJ02Point(23.110319308993134, 113.30138024838311, 3.14, "foo bar"),
            BD09MCPoint(2629182.88, 12613508.26, 3.14, "foo bar").toGCJ02()
        )
        assertEquals(
            GCJ02Point(23.146380831856163, 113.30063234845544, 3.14, "foo bar"),
            BD09MCPoint(2633524.681382545, 12613424.449999997, 3.14, "foo bar").toGCJ02()
        )
    }
}
