package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.FileType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.mockGeometries

class OpenPointsGpxOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val log = FakeLog
    private val activity = FileActivity(PackageNames.TEST, FileType.GPX)
    private val output = OpenPointsGpxOutput(activity, coordinateConverter, log)

    @Test
    fun write_whenPointsContainThreePoints_writesGpxWaypoints() = runTest {
        val stringBuilder = StringBuilder()
        val points = persistentListOf(
            WGS84Point(3.0, 4.0, source = Source.GENERATED),
            WGS84Point(5.0, 6.0, name = "My waypoint", source = Source.GENERATED),
            WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED),
        )
        output.write(points, stringBuilder)
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="3" lon="4" />
<wpt lat="5" lon="6">
    <name>My waypoint</name>
</wpt>
<wpt lat="1" lon="2">
    <name>My destination</name>
</wpt>
</gpx>
""",
            stringBuilder.toString(),
        )
    }

}
