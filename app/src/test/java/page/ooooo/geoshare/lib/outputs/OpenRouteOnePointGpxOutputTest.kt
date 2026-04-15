package page.ooooo.geoshare.lib.outputs

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
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.GeoTest
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import java.io.File
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempDirectory

class OpenRouteOnePointGpxOutputTest : GeoTest {
    private val geometries = mockGeometries()
    private val coordinateConverter = CoordinateConverter(geometries)

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
                on { openAppFile(any(), eq(PackageNames.TEST), any()) } doReturn true
                on { openChooser(any(), any()) } doThrow NotImplementedError()
                on { openChooserFile(any(), any()) } doThrow NotImplementedError()
            },
        )

    @Test
    fun execute_locationIsNull_returnsFalse() = runTest {
        val parentDir = createTempDirectory().toFile()
        val success = OpenRouteOnePointGpxOutput(PackageNames.TEST, coordinateConverter).execute(
            location = null,
            value = WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED),
            actionContext = mockActionContext(parentDir),
        )
        assertFalse(success)
    }

    @Test
    fun execute_parentDirIsNotWritable_returnsFalse() = runTest {
        val parentDir = createTempDirectory(
            null,
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--------")),
        ).toFile()
        val success = OpenRouteOnePointGpxOutput(PackageNames.TEST, coordinateConverter).execute(
            location = WGS84Point(3.0, 4.0, source = Source.GPS_SENSOR),
            value = WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED),
            actionContext = mockActionContext(parentDir),
        )
        assertFalse(success)
    }

    @Test
    fun execute_pointIsPassed_deletesRoutesDirAndWritesToItAGpxRouteFromLocationToPoint() = runTest {
        val parentDir = createTempDirectory().toFile()
        val childDir = File(parentDir, "routes")
        childDir.mkdirs()
        val oldFile = File(childDir, "000.xml")
        oldFile.writeText("<gpx></gpx>")
        assertEquals(
            setOf(oldFile.path),
            childDir.listFiles()?.map { it.path }?.toSet(),
        )
        val success = OpenRouteOnePointGpxOutput(PackageNames.TEST, coordinateConverter).execute(
            location = WGS84Point(3.0, 4.0, source = Source.GPS_SENSOR),
            value = WGS84Point(1.0, 2.0, name = "My destination", source = Source.GENERATED),
            actionContext = mockActionContext(parentDir),
        )
        assertTrue(success)
        val resFiles = childDir.listFiles()
        assertEquals(1, resFiles?.size)
        val resFile = resFiles?.first()
        assertFalse(resFile?.path == oldFile.path)
        assertEquals(
            @Suppress("SpellCheckingInspection")
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
            resFile?.readText(),
        )
    }
}
