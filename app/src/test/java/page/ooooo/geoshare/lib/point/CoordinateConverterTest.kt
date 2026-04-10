package page.ooooo.geoshare.lib.point

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.geo.ChinaGeometryTest

class CoordinateConverterTest {
    private val mockAssetManager: AssetManager = mock {
        on { open("china.wkb") } doReturn
            (ChinaGeometryTest::class.java.getResourceAsStream("/china.wkb")
                ?: error("china.wkb not found in test resources"))
    }
    private val mockContext: Context = mock {
        on { assets } doReturn mockAssetManager
    }
    private val chinaGeometry = ChinaGeometry(mockContext)
    private val coordinateConverter = CoordinateConverter(chinaGeometry)

    @Test
    fun toGCJ02_whenPointIsWGS84AndDoesNotHaveCoords_returnsGCJ02PointWithoutCoords() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(WGS84Point(z = 3.14, name = "foo bar", source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndInChina_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(31.22281206362763, 121.46840659541449, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                WGS84Point(31.224731304675522, 121.46385323166844, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndInChinaNearCoast_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(30.60283, 122.12886, source = Source.GENERATED), // TODO This should be converted to GCJ-02
            coordinateConverter.toGCJ02(WGS84Point(30.60283, 122.12886, source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndNotInChina_returnsGCJ02PointWithUnchangedCoords() {
        assertEquals(
            GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndDoesNotHaveCoords_returnsWGS84PointWithoutCoords() {
        assertEquals(
            WGS84Point(z = 3.14, name = "foo bar", source = Source.GENERATED),
            coordinateConverter.toWGS84(GCJ02Point(z = 3.14, name = "foo bar", source = Source.GENERATED)),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndInChina_returnsWGS84PointWithConvertedCoords() {
        assertEquals(
            WGS84Point(31.224731304675522, lon = 121.46385323166844, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toWGS84(
                GCJ02Point(31.222811749011463, 121.46840706467624, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndNotInChina_returnsWGS84PointWithUnchangedCoords() {
        assertEquals(
            WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toWGS84(
                GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02_whenPointIsBD09MCAndDoesNotHaveCoords_returnsGCJ02PointWithoutCoords() {
        assertEquals(
            GCJ02Point(z = 3.14, name = "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(BD09MCPoint(z = 3.14, name = "foo bar", source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02_whenPointIsBD09MCAndInChina_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(28.696786436412197, 121.45032959369264, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                BD09MCPoint(3317203.0, 13520653.0, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
        assertEquals(
            GCJ02Point(28.686779688493015, 121.29095727245614, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                BD09MCPoint(3315902.2199999997, 13502918.375, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
        assertEquals(
            GCJ02Point(23.110319308993134, 113.30138024838311, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                BD09MCPoint(2629182.88, 12613508.26, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
        assertEquals(
            GCJ02Point(23.146380831856163, 113.30063234845544, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                BD09MCPoint(2633524.681382545, 12613424.449999997, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }
}
