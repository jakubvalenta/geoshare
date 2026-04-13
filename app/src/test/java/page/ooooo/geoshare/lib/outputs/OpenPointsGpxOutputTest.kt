package page.ooooo.geoshare.lib.outputs

import android.content.Context
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.android.TEST_PACKAGE_NAME
import page.ooooo.geoshare.lib.formatters.GpxFormatter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import java.io.File
import kotlin.io.path.createTempDirectory

class OpenPointsGpxOutputTest {
    private val mockContext: Context = mock {}
    private val geometries = Geometries(mockContext)
    private val coordinateConverter = CoordinateConverter(geometries)
    private val gpxFormatter = GpxFormatter(coordinateConverter)

    private fun mockActionContext(parentDir: File): ActionContext =
        ActionContext(
            context = mock {
                on { filesDir } doReturn parentDir
            },
            clipboard = mock {},
            resources = mock {},
            androidTools = mock {
                on { openApp(any(), any(), any()) } doThrow NotImplementedError()
                on { openAppFile(any(), any(), any()) } doThrow NotImplementedError()
                on { openAppFile(any(), eq(TEST_PACKAGE_NAME), any()) } doReturn true
                on { openChooser(any(), any()) } doThrow NotImplementedError()
                on { openChooserFile(any(), any()) } doThrow NotImplementedError()
            },
        )

    @Test
    fun execute_pointsHasThreePoints_writesGpxPoints() = runTest {
        val points = persistentListOf(
            WGS84Point(3.0, 4.0, source = Source.GENERATED),
            WGS84Point(5.0, 6.0, name = "My waypoint", source = Source.GENERATED),
            WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED),
        )
        val parentDir = createTempDirectory().toFile()
        val childDir = File(parentDir, "points")
        childDir.mkdirs()
        val oldFile = File(childDir, "000.xml")
        oldFile.writeText("<gpx></gpx>")
        assertEquals(
            setOf(oldFile.path),
            childDir.listFiles()?.map { it.path }?.toSet(),
        )
        val success = OpenPointsGpxOutput(TEST_PACKAGE_NAME, gpxFormatter).execute(
            value = points,
            actionContext = mockActionContext(parentDir),
        )
        assertTrue(success)
        val resFiles = childDir.listFiles()
        assertEquals(1, resFiles?.size)
        val resFile = resFiles?.first()
        assertFalse(resFile?.path == oldFile.path)
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
            resFile?.readText(),
        )
    }

}
