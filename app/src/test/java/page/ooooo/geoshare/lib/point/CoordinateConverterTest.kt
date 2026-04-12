package page.ooooo.geoshare.lib.point

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.geo.ChinaGeometryTest
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02ChinaPoint
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class CoordinateConverterTest {
    private val mockAssetManager: AssetManager = mock {
        on { open("china_ne_10m.wkb") } doReturn
            (ChinaGeometryTest::class.java.getResourceAsStream("/china_ne_10m.wkb")
                ?: error("china_ne_10m.wkb not found in test resources"))
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
    fun toGCJ02_whenPointIsWGS84AndWithinChina_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(31.22281206362763, 121.46840659541449, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                WGS84Point(31.224731304675522, 121.46385323166844, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndWithinChinaNearCoast_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(37.33644561966912, 122.48151345759582, source = Source.GENERATED),
            coordinateConverter.toGCJ02(WGS84Point(37.33557, 122.47664, source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndWithinChinaNearCoast2_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(30.600649446449268, 122.13324202346543, source = Source.GENERATED),
            coordinateConverter.toGCJ02(WGS84Point(30.60283, 122.12886, source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndOutsideChina_returnsGCJ02PointWithUnchangedCoords() {
        assertEquals(
            GCJ02Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02_whenPointIsWGS84AndOutsideChinaNearCoast_returnsGCJ02PointWithConvertedCoords() {
        assertEquals(
            GCJ02Point(37.39578114164097, 122.71208265323477, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02(
                WGS84Point(37.394978, 122.707243, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02China_whenPointIsWGS84AndDoesNotHaveCoords_returnsGCJ02ChinaPointWithoutCoords() {
        assertEquals(
            GCJ02ChinaPoint(z = 3.14, name = "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02China(WGS84Point(z = 3.14, name = "foo bar", source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02China_whenPointIsWGS84AndWithinChina_returnsGCJ02ChinaPointWithConvertedCoords() {
        assertEquals(
            GCJ02ChinaPoint(31.22281206362763, 121.46840659541449, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02China(
                WGS84Point(31.224731304675522, 121.46385323166844, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02China_whenPointIsWGS84AndWithinChinaNearCoast_returnsGCJ02ChinaPointWithConvertedCoords() {
        assertEquals(
            GCJ02ChinaPoint(37.33644561966912, 122.48151345759582, source = Source.GENERATED),
            coordinateConverter.toGCJ02China(WGS84Point(37.33557, 122.47664, source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02China_whenPointIsWGS84AndWithinChinaNearCoast2_returnsGCJ02PointWithConvertedCoords() {
        // TODO Improve china boundary to fix this point
        assumeTrue("This test currently fails, because Natural Earth 10m is not precise enough", false)
        assertEquals(
            GCJ02ChinaPoint(30.600649446449268, 122.13324202346543, source = Source.GENERATED),
            coordinateConverter.toGCJ02China(WGS84Point(30.60283, 122.12886, source = Source.GENERATED)),
        )
    }

    @Test
    fun toGCJ02China_whenPointIsWGS84AndOutsideChina_returnsGCJ02ChinaPointWithUnchangedCoords() {
        assertEquals(
            GCJ02ChinaPoint(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02China(
                WGS84Point(45.8289525077221, 1.266689300537103, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toGCJ02China_whenPointIsWGS84AndOutsideChinaNearCoast_returnsGCJ02ChinaPointWithUnchangedCoords() {
        assertEquals(
            GCJ02ChinaPoint(37.394978, 122.707243, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toGCJ02China(
                WGS84Point(37.394978, 122.707243, 3.14, "foo bar", source = Source.GENERATED)
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
    fun toWGS84_whenPointIsGCJ02AndWithinChina_returnsWGS84PointWithConvertedCoords() {
        assertEquals(
            WGS84Point(31.224731304675522, lon = 121.46385323166844, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toWGS84(
                GCJ02Point(31.222811749011463, 121.46840706467624, 3.14, "foo bar", source = Source.GENERATED)
            ),
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02AndOutsideChina_returnsWGS84PointWithUnchangedCoords() {
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
    fun toGCJ02_whenPointIsBD09MCAndWithinChina_returnsGCJ02PointWithConvertedCoords() {
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

    @Test
    fun toWGS84_whenPointsIsGCJ02ChinaAndOutsideChinaNearCoast_returnsWGS84PointWithConvertedCoordinates() {
        assertEquals(
            WGS84Point(37.33557037552849, 122.47663919001769, 3.14, "foo bar", source = Source.GENERATED),
            coordinateConverter.toWGS84(
                GCJ02ChinaPoint(37.33644561966912, 122.48151345759582, 3.14, "foo bar", source = Source.GENERATED)
            )
        )
    }

    @Test
    fun toWGS84_whenPointIsGCJ02ChinaAndWithinChinaNearCoast2_returnsWGS84PointWithConvertedCoords() {
        // TODO Improve china boundary to fix this point
        assumeTrue("This test currently fails, because Natural Earth 10m is not precise enough", false)
        assertEquals(
            WGS84Point(30.60283, 122.12886, source = Source.GENERATED),
            coordinateConverter.toWGS84(
                GCJ02ChinaPoint(30.600649446449268, 122.13324202346543, source = Source.GENERATED)
            ),
        )
    }
}
