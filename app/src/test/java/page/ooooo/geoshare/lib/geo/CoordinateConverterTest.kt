package page.ooooo.geoshare.lib.geo

import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.lib.extensions.toScale
import kotlin.math.roundToLong

class CoordinateConverterTest : GeoTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries())

    private data class PointInDifferentSrs(
        val name: String,
        val wgs84: WGS84Point? = null,
        val gcj02: GCJ02Point? = null,
        val gcj02MainlandChina: GCJ02MainlandChinaPoint? = null,
        val gcj02GreaterChinaAndTaiwan: GCJ02GreaterChinaAndTaiwanPoint? = null,
        val bd09MC: BD09MCPoint? = null,
    )

    private val points = listOf(
        PointInDifferentSrs(
            name = "Empty point",
            wgs84 = WGS84Point(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Limoges",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                45.8289525077221, 1.266689300537103,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Shanghai center",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                31.22281206362763, 121.46840659541449,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Rongcheng inland",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                37.33644561966912, 122.48151345759582,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Rongcheng sea",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                37.39578114164097, 122.71208265323477,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = @Suppress("SpellCheckingInspection") "Yangshan port island",
            wgs84 = WGS84Point(
                30.602829622230516, 122.12885969924668,
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                30.600649446449268, 122.13324202346543,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = @Suppress("SpellCheckingInspection") "Daqindao sea",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                38.30121472559038, 120.81016239968592,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Hong Kong",
            wgs84 = WGS84Point(
                22.301015146333217, 114.17126075831801,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                22.298266637305655, 114.17622169254186,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                22.301015146333217, 114.17126075831801,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                22.298266637305655, 114.17622169254186,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Macao",
            wgs84 = WGS84Point(
                22.18843530811167, 113.54346458759652,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02 = GCJ02Point(
                22.185500127447607, 113.54856895771371,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02MainlandChina = GCJ02MainlandChinaPoint(
                22.18843530811167, 113.54346458759652,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                22.185500127447607, 113.54856895771371,
                z = 3.14, name = "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Taiwan",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                25.08380369719241, 121.51320397853848,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "Western Japan",
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
            gcj02GreaterChinaAndTaiwan = GCJ02GreaterChinaAndTaiwanPoint(
                34.36875865823159, 131.1821490526199,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "BD09MC point 1",
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
            name = "BD09MC point 2",
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
            name = "BD09MC point 3",
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
            name = "BD09MC point 4",
            gcj02 = GCJ02Point(
                23.146380831856163, 113.30063234845544,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                2633524.681382545, 12613424.449999997,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
        PointInDifferentSrs(
            name = "BD09MC Eastern Japan",
            gcj02 = GCJ02Point(
                43.32229116408107, 145.58037496052287,
                3.14, "foo bar", source = Source.GENERATED,
            ),
            bd09MC = BD09MCPoint(
                5332511.04, 16206826.38,
                3.14, "foo bar", source = Source.GENERATED,
            ),
        ),
    )

    private fun <T : Point> assertPointsEqual(expectedPoint: T, actualPoint: T, name: String) =
        Assert.assertTrue(
            "Expected $name $expectedPoint to equal $actualPoint",
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
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.wgs84), point.name)
            }
        }
    }

    @Test
    fun toWGS84_fromGCJ02() {
        for (point in points) {
            if (point.wgs84 != null && point.gcj02 != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.gcj02), point.name)
            }
        }
    }

    @Test
    fun toWGS84_fromGCJ02China() {
        for (point in points) {
            if (point.wgs84 != null && point.gcj02MainlandChina != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.gcj02MainlandChina), point.name)
            }
        }
    }

    @Test
    fun toWGS84_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.wgs84 != null && point.gcj02GreaterChinaAndTaiwan != null) {
                assertPointsEqual(
                    point.wgs84,
                    coordinateConverter.toWGS84(point.gcj02GreaterChinaAndTaiwan),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toWGS84_fromBD09MC() {
        for (point in points) {
            if (point.wgs84 != null && point.bd09MC != null) {
                assertPointsEqual(point.wgs84, coordinateConverter.toWGS84(point.bd09MC), point.name)
            }
        }
    }

    @Test
    fun toGCJ02_fromWGS84() {
        for (point in points) {
            if (point.gcj02 != null && point.wgs84 != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.wgs84), point.name)
            }
        }
    }

    @Test
    fun toGCJ02_fromGCJ02() {
        for (point in points) {
            if (point.gcj02 != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.gcj02), point.name)
            }
        }
    }

    @Test
    fun toGCJ02_fromGCJ02China() {
        for (point in points) {
            if (point.gcj02 != null && point.gcj02MainlandChina != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.gcj02MainlandChina), point.name)
            }
        }
    }

    @Test
    fun toGCJ02_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.gcj02 != null && point.gcj02GreaterChinaAndTaiwan != null) {
                assertPointsEqual(
                    point.gcj02,
                    coordinateConverter.toGCJ02(point.gcj02GreaterChinaAndTaiwan),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02_fromBD09MC() {
        for (point in points) {
            if (point.gcj02 != null && point.bd09MC != null) {
                assertPointsEqual(point.gcj02, coordinateConverter.toGCJ02(point.bd09MC), point.name)
            }
        }
    }

    @Test
    fun toGCJ02MainlandChina_fromWGS84() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.wgs84 != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.wgs84),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02MainlandChina_fromGCJ02() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.gcj02 != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.gcj02),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02MainlandChina_fromGCJ02China() {
        for (point in points) {
            if (point.gcj02MainlandChina != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.gcj02MainlandChina),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02MainlandChina_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.gcj02GreaterChinaAndTaiwan != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.gcj02GreaterChinaAndTaiwan),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02MainlandChina_fromBD09MC() {
        for (point in points) {
            if (point.gcj02MainlandChina != null && point.bd09MC != null) {
                assertPointsEqual(
                    point.gcj02MainlandChina,
                    coordinateConverter.toGCJ02MainlandChina(point.bd09MC),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02GreaterChinaAndTaiwan_fromWGS84() {
        for (point in points) {
            if (point.gcj02GreaterChinaAndTaiwan != null && point.wgs84 != null) {
                assertPointsEqual(
                    point.gcj02GreaterChinaAndTaiwan,
                    coordinateConverter.toGCJ02GreaterChinaAndTaiwan(point.wgs84),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02GreaterChinaAndTaiwan_fromGCJ02() {
        for (point in points) {
            if (point.gcj02GreaterChinaAndTaiwan != null && point.gcj02 != null) {
                assertPointsEqual(
                    point.gcj02GreaterChinaAndTaiwan,
                    coordinateConverter.toGCJ02GreaterChinaAndTaiwan(point.gcj02),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02GreaterChinaAndTaiwan_fromGCJ02China() {
        for (point in points) {
            if (point.gcj02GreaterChinaAndTaiwan != null && point.gcj02MainlandChina != null) {
                assertPointsEqual(
                    point.gcj02GreaterChinaAndTaiwan,
                    coordinateConverter.toGCJ02GreaterChinaAndTaiwan(point.gcj02MainlandChina),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02GreaterChinaAndTaiwan_fromGCJ02ChinaAndTaiwan() {
        for (point in points) {
            if (point.gcj02GreaterChinaAndTaiwan != null) {
                assertPointsEqual(
                    point.gcj02GreaterChinaAndTaiwan,
                    coordinateConverter.toGCJ02GreaterChinaAndTaiwan(point.gcj02GreaterChinaAndTaiwan),
                    point.name,
                )
            }
        }
    }

    @Test
    fun toGCJ02GreaterChinaAndTaiwan_fromBD09MC() {
        for (point in points) {
            if (point.gcj02GreaterChinaAndTaiwan != null && point.bd09MC != null) {
                assertPointsEqual(
                    point.gcj02GreaterChinaAndTaiwan,
                    coordinateConverter.toGCJ02GreaterChinaAndTaiwan(point.bd09MC),
                    point.name,
                )
            }
        }
    }
}
