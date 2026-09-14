package page.ooooo.geoshare.lib.outputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.FileType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.geo.mockGeometries

class OpenRouteOnePointGpxOutputTest {
    private val coordinateConverter = CoordinateConverter(mockGeometries)
    private val log = FakeLog
    private val activity = FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT)
    private val output = OpenRouteOnePointGpxOutput(activity, coordinateConverter, log)

    @Test
    fun execute_whenLocationIsNull_returnsFailed() = runTest {
        val actionResult = output.execute(
            location = null,
            value = WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED),
            actionContext = mock(),
        )
        assertEquals(ActionResult.FAILED, actionResult)
    }

    @Test
    fun write_whenLocationAndPointArePassed_writesGpxRouteFromLocationToPoint() = runTest {
        val stringBuilder = StringBuilder()
        val location = WGS84Point(3.0, 4.0, source = Source.GPS_SENSOR)
        val point = WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED)
        output.write(location, point, stringBuilder)
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<rte>
<rtept lat="3" lon="4" />
<rtept lat="1" lon="2">
    <name>My destination</name>
</rtept>
</rte>
</gpx>
""",
            stringBuilder.toString(),
        )
    }
}
