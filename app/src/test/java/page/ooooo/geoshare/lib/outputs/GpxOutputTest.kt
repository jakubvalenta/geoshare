package page.ooooo.geoshare.lib.outputs

import junit.framework.TestCase.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import java.io.File
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempDirectory

class GpxOutputTest {
    private val output = GpxOutput

    @Test
    fun writeGpxRoute_locationIsNull_returnsNull() {
        val position = Position(Srs.WGS84, 1.0, 2.0, name = "My destination")
        val location = null
        val parentDir = createTempDirectory().toFile()
        assertNull(output.writeGpxRoute(position, 0, location, parentDir))
    }

    @Test
    fun writeGpxRoute_positionHasNoPoints_returnsNull() {
        val position = Position(points = persistentListOf())
        val location = Point(Srs.WGS84, 3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        assertNull(output.writeGpxRoute(position, 0, location, parentDir))
    }

    @Test
    fun writeGpxRoute_positionDoesNotHavePointIndex_returnsNull() {
        val position = Position(Srs.WGS84, 1.0, 2.0, name = "My destination")
        val location = Point(Srs.WGS84, 3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        assertNull(output.writeGpxRoute(position, 1, location, parentDir))
    }

    @Test
    fun writeGpxRoute_parentDirIsNotWritable_returnsNull() {
        val position = Position(Srs.WGS84, 1.0, 2.0, name = "My destination")
        val location = Point(Srs.WGS84, 3.0, 4.0)
        val parentDir = createTempDirectory(
            null,
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--------")),
        ).toFile()
        assertNull(output.writeGpxRoute(position, 0, location, parentDir))
    }

    @Test
    fun writeGpxRoute_deletesRoutesDirAndWritesToItAGpxRouteFromLocationToMainPoint() {
        val position = Position(Srs.WGS84, 1.0, 2.0, name = "My destination")
        val location = Point(Srs.WGS84, 3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        val dir = File(parentDir, "routes")
        dir.mkdirs()
        val old = File(dir, "000.xml")
        old.writeText("<gpx></gpx>")
        assertEquals(
            setOf(old.path),
            dir.listFiles()?.map { it.path }?.toSet(),
        )
        val result = output.writeGpxRoute(position, 0, location, parentDir)
        assertEquals(
            setOf(result?.path),
            dir.listFiles()?.map { it.path }?.toSet(),
        )
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
            result?.readText(),
        )
    }
}
