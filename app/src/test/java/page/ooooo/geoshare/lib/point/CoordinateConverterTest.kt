package page.ooooo.geoshare.lib.point

import android.content.Context
import android.content.res.AssetManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.extensions.toScale
import page.ooooo.geoshare.lib.geo.BD09MCPoint
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaAndTaiwanPoint
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.GeometriesTest
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import kotlin.math.roundToLong

class CoordinateConverterTest {
    private val mockAssetManager: AssetManager = mock {
        on { open("mainland_china.wkb") } doReturn
            (GeometriesTest::class.java.getResourceAsStream("/mainland_china.wkb")
                ?: error("mainland_china.wkb not found in test resources"))
        on { open("taiwan.wkb") } doReturn
            (GeometriesTest::class.java.getResourceAsStream("/taiwan.wkb")
                ?: error("taiwan.wkb not found in test resources"))
    }
    private val mockContext: Context = mock {
        on { assets } doReturn mockAssetManager
    }
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)

    private data class PointInDifferentSrs(
        val wgs84: WGS84Point? = null,
        val gcj02: GCJ02Point? = null,
        val gcj02MainlandChina: GCJ02MainlandChinaPoint? = null,
        val gcj02MainlandChinaAndTaiwan: GCJ02MainlandChinaAndTaiwanPoint? = null,
        val bd09MC: BD09MCPoint? = null,
    )

    private val points = listOf(
        // Empty point
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        // Limoges
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                45.8289525077221, 1.266689300537103,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                45.8289525077221, 1.266689300537103,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                45.8289525077221, 1.266689300537103,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                45.8289525077221, 1.266689300537103,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        // Shanghai center
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                31.224731304675522, 121.46385323166844,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                31.22281206362763, 121.46840659541449,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                31.22281206362763, 121.46840659541449,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                31.22281206362763, 121.46840659541449,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        // Rongcheng inland
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                37.33557, 122.47664,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                37.33644561966912, 122.48151345759582,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                37.33644561966912, 122.48151345759582,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                37.33644561966912, 122.48151345759582,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        // Rongcheng sea
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                37.394978, 122.707243,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                37.39578114164097, 122.71208265323477,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                37.39578114164097, 122.71208265323477,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                37.39578114164097, 122.71208265323477,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        @Suppress("SpellCheckingInspection")
        // Yangshan port island
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                30.60283, 122.12886,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                30.600649446449268, 122.13324202346543,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                30.600649446449268, 122.13324202346543,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                30.600649446449268, 122.13324202346543,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        @Suppress("SpellCheckingInspection")
        // Daqindao sea
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                38.30050979122315, 120.80518963762754,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                38.30121472559038, 120.81016239968592,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                38.30121472559038, 120.81016239968592,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                38.30121472559038, 120.81016239968592,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        // Taiwan
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                25.086597886645535, 121.50927209377286,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                25.08380369719241, 121.51320397853848,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                25.086597886645535, 121.50927209377286,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                25.08380369719241, 121.51320397853848,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        // Western Japan
        PointInDifferentSrs(
            wgs84 = WGS84Point(
                34.36875865823159, 131.1821490526199,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                34.36783913297475, 131.18823621449667,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                34.36875865823159, 131.1821490526199,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChinaAndTaiwan = GCJ02MainlandChinaAndTaiwanPoint(
                34.36875865823159, 131.1821490526199,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        // BD09MC points
        PointInDifferentSrs(
            gcj02 = GCJ02Point(
                28.696786436412197, 121.45032959369264,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                3317203.0, 13520653.0,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            gcj02 = GCJ02Point(
                28.686779688493015, 121.29095727245614,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                3315902.2199999997, 13502918.375,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            gcj02 = GCJ02Point(
                23.110319308993134, 113.30138024838311,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                2629182.88, 12613508.26,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            gcj02 = GCJ02Point(
                23.146380831856163, 113.30063234845544,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                2633524.681382545, 12613424.449999997,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        // TODO Test bd09mc in eastern Japan
    )

    private fun <T : Point> assertPointsEqual(expectedPoint: T, actualPoint: T) =
        assertTrue(
            "Expected $expectedPoint to equal $actualPoint",
            when (expectedPoint) {
                is BD09MCPoint ->
                    expectedPoint.lat?.roundToLong() == actualPoint.lat?.roundToLong() &&
                        expectedPoint.lon?.roundToLong() == actualPoint.lon?.roundToLong()

                else ->
                    expectedPoint.lat?.toScale(5) == actualPoint.lat?.toScale(5) &&
                        expectedPoint.lon?.toScale(5) == actualPoint.lon?.toScale(5)
            } &&
                expectedPoint.z == actualPoint.z &&
                expectedPoint.name == actualPoint.name &&
                expectedPoint.source == actualPoint.source,
        )

    @Test
    fun toWGS84_fromWGS84() {
        for (point in points) {
            if (point.wgs84 != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.wgs84))
            }
        }
    }

    @Test
    fun toWGS84_fromGCJ02() {
        for (point in points) {
            if (point.wgs84 != null && point.gcj02 != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.gcj02))
            }
        }
    }

    @Test
    fun toWGS84_fromGCJ02China() {
        for (point in points) {
            if (point.wgs84 != null && point.gcj02MainlandChina != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.gcj02MainlandChina))
            }
        }
    }

    @Test
    fun toWGS84_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.wgs84 != null && point.gcj02MainlandChinaAndTaiwan != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.gcj02MainlandChinaAndTaiwan))
            }
        }
    }

    @Test
    fun toWGS84_fromBD09MC() {
        for (point in points) {
            if (point.wgs84 != null && point.bd09MC != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.bd09MC))
            }
        }
    }

    @Test
    fun toGCJ02_fromWGS84() {
        for (point in points) {
            if (point.gcj02 != null && point.wgs84 != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.wgs84))
            }
        }
    }

    @Test
    fun toGCJ02_fromGCJ02() {
        for (point in points) {
            if (point.gcj02 != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.gcj02))
            }
        }
    }

    @Test
    fun toGCJ02_fromGCJ02China() {
        for (point in points) {
            if (point.gcj02 != null && point.gcj02MainlandChina != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.gcj02MainlandChina))
            }
        }
    }

    @Test
    fun toGCJ02_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.gcj02 != null && point.gcj02MainlandChinaAndTaiwan != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.gcj02MainlandChinaAndTaiwan))
            }
        }
    }

    @Test
    fun toGCJ02_fromBD09MC() {
        for (point in points) {
            if (point.gcj02 != null && point.bd09MC != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.bd09MC))
            }
        }
    }

    @Test
    fun toGCJ02China_fromWGS84() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.wgs84 != null) {
                assertPointsEqual(point.gcj02MainlandChina, coordinateConverter.toGCJ02MainlandChina(point.wgs84))
            }
        }
    }

    @Test
    fun toGCJ02China_fromGCJ02() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.gcj02 != null) {
                assertPointsEqual(point.gcj02MainlandChina, coordinateConverter.toGCJ02MainlandChina(point.gcj02))
            }
        }
    }

    @Test
    fun toGCJ02China_fromGCJ02China() {
        for (point in points) {
            if (point.gcj02MainlandChina != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.gcj02MainlandChina)
                )
            }
        }
    }

    @Test
    fun toGCJ02China_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.gcj02MainlandChinaAndTaiwan != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.gcj02MainlandChinaAndTaiwan)
                )
            }
        }
    }

    @Test
    fun toGCJ02China_fromBD09MC() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.bd09MC != null) {
                assertPointsEqual(point.gcj02MainlandChina, coordinateConverter.toGCJ02MainlandChina(point.bd09MC))
            }
        }
    }

    @Test
    fun toGCJ02ChinaAndTaiwan_fromWGS84() {
        for (point in points) {
            if (point.gcj02MainlandChinaAndTaiwan != null && point.wgs84 != null) {
                assertPointsEqual(
                    point.gcj02MainlandChinaAndTaiwan,
                    coordinateConverter.toGCJ02MainlandChinaAndTaiwan(point.wgs84)
                )
            }
        }
    }

    @Test
    fun toGCJ02ChinaAndTaiwan_fromGCJ02() {
        for (point in points) {
            if (point.gcj02MainlandChinaAndTaiwan != null && point.gcj02 != null) {
                assertPointsEqual(
                    point.gcj02MainlandChinaAndTaiwan,
                    coordinateConverter.toGCJ02MainlandChinaAndTaiwan(point.gcj02)
                )
            }
        }
    }

    @Test
    fun toGCJ02ChinaAndTaiwan_fromGCJ02China() {
        for (point in points) {
            if (point.gcj02MainlandChinaAndTaiwan != null && point.gcj02MainlandChina != null) {
                assertPointsEqual(
                    point.gcj02MainlandChinaAndTaiwan,
                    coordinateConverter.toGCJ02MainlandChinaAndTaiwan(point.gcj02MainlandChina)
                )
            }
        }
    }

    @Test
    fun toGCJ02ChinaAndTaiwan_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.gcj02MainlandChinaAndTaiwan != null) {
                assertPointsEqual(
                    point.gcj02MainlandChinaAndTaiwan,
                    coordinateConverter.toGCJ02MainlandChinaAndTaiwan(point.gcj02MainlandChinaAndTaiwan)
                )
            }
        }
    }

    @Test
    fun toGCJ02ChinaAndTaiwan_fromBD09MC() {
        for (point in points) {
            if (point.gcj02MainlandChinaAndTaiwan != null && point.bd09MC != null) {
                assertPointsEqual(
                    point.gcj02MainlandChinaAndTaiwan,
                    coordinateConverter.toGCJ02MainlandChinaAndTaiwan(point.bd09MC)
                )
            }
        }
    }
}
