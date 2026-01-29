package page.ooooo.geoshare.lib.point

import org.junit.Assert.assertEquals
import org.junit.Test

class BD09MCPointTest {

    @Test
    fun toGCJ02_convertsBD09MCCoordinates() {
        assertEquals(
            GCJ02Point(28.696786436412197, 121.45032959369264),
            BD09MCPoint(3317203.0, 13520653.0).toGCJ02()
        )
        assertEquals(
            GCJ02Point(28.686779688493015, 121.29095727245614),
            BD09MCPoint(3315902.2199999997, 13502918.375).toGCJ02()
        )
        assertEquals(
            GCJ02Point(23.110319308993134, 113.30138024838311),
            BD09MCPoint(2629182.88, 12613508.26).toGCJ02()
        )
        assertEquals(
            GCJ02Point(23.146380831856163, 113.30063234845544),
            BD09MCPoint(2633524.681382545, 12613424.449999997).toGCJ02()
        )
    }
}
