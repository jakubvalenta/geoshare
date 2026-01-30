package page.ooooo.geoshare.lib.outputs

import junit.framework.TestCase.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point
import java.io.File
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempDirectory

class GpxOutputTest {
    private val output = GpxOutput

    @Test
    fun writeGpxRoute_locationIsNull_returnsNull() {
        val points = persistentListOf(WGS84Point(1.0, 2.0, name = "My destination"))
        val location = null
        val parentDir = createTempDirectory().toFile()
        assertNull(output.writeGpxRoute(points, null, location, parentDir))
    }

    @Test
    fun writeGpxRoute_pointsHasNoPoints_returnsNull() {
        val points = persistentListOf<WGS84Point>()
        val location = WGS84Point(3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        assertNull(output.writeGpxRoute(points, null, location, parentDir))
    }

    @Test
    fun writeGpxRoute_pointsDoesNotHavePointIndex_returnsNull() {
        val points = persistentListOf(WGS84Point(1.0, 2.0, name = "My destination"))
        val location = WGS84Point(3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        assertNull(output.writeGpxRoute(points, 1, location, parentDir))
    }

    @Test
    fun writeGpxRoute_parentDirIsNotWritable_returnsNull() {
        val points = persistentListOf(WGS84Point(1.0, 2.0, name = "My destination"))
        val location = WGS84Point(3.0, 4.0)
        val parentDir = createTempDirectory(
            null,
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--------")),
        ).toFile()
        assertNull(output.writeGpxRoute(points, null, location, parentDir))
    }

    @Test
    fun writeGpxRoute_pointsHasOnePoint_deletesRoutesDirAndWritesToItAGpxRouteFromLocationToMainPoint() {
        val points = persistentListOf(WGS84Point(1.0, 2.0, name = "My destination"))
        val location = WGS84Point(3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        val dir = File(parentDir, "routes")
        dir.mkdirs()
        val old = File(dir, "000.xml")
        old.writeText("<gpx></gpx>")
        assertEquals(
            setOf(old.path),
            dir.listFiles()?.map { it.path }?.toSet(),
        )
        val result = output.writeGpxRoute(points, null, location, parentDir)
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

    @Test
    fun writeGpxRoute_pointsHasTwoPointsAndPointIndexIsNull_writesGpxRouteFromLocationToMainPointViaWaypoint() {
        val points = persistentListOf(
            WGS84Point(5.0, 6.0, name = "My waypoint"),
            WGS84Point(1.0, 2.0, name = "My destination"),
        )
        val location = WGS84Point(3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        val dir = File(parentDir, "routes")
        dir.mkdirs()
        val old = File(dir, "000.xml")
        old.writeText("<gpx></gpx>")
        assertEquals(
            setOf(old.path),
            dir.listFiles()?.map { it.path }?.toSet(),
        )
        val result = output.writeGpxRoute(points, null, location, parentDir)
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
<rtept lat="5" lon="6">
    <name>My waypoint</name>
</rtept>
<rtept lat="1" lon="2">
    <name>My destination</name>
</rtept>
</rte>
</gpx>
""",
            result?.readText(),
        )
    }

    @Test
    fun writeGpxRoute_pointsHasTwoPointsAndPointIndexIsNotNull_writesGpxRouteFromLocationToPointByIndex() {
        val points = persistentListOf(
            WGS84Point(5.0, 6.0, name = "My waypoint"),
            WGS84Point(1.0, 2.0, name = "My destination"),
        )
        val location = WGS84Point(3.0, 4.0)
        val parentDir = createTempDirectory().toFile()
        val dir = File(parentDir, "routes")
        dir.mkdirs()
        val old = File(dir, "000.xml")
        old.writeText("<gpx></gpx>")
        assertEquals(
            setOf(old.path),
            dir.listFiles()?.map { it.path }?.toSet(),
        )
        val result = output.writeGpxRoute(points, 0, location, parentDir)
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
<rtept lat="5" lon="6">
    <name>My waypoint</name>
</rtept>
</rte>
</gpx>
""",
            result?.readText(),
        )
    }
}
