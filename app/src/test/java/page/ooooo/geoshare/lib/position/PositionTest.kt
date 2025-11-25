package page.ooooo.geoshare.lib.position

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionTest {
    @Test
    fun writeGpxPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="50.123456" lon="-11.123456" />
<wpt lat="52.5067296" lon="13.2599309" />
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point(Srs.WGS84, 50.123456, -11.123456),
                        Point(Srs.WGS84, 52.5067296, 13.2599309),
                    ),
                ).writeGpxPoints(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxPoints_escapesName() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="50.123456" lon="-11.123456">
    <name>&lt;script&gt;alert()&lt;/script&gt;</name>
</wpt>
</gpx>
""",
            StringBuilder().apply {
                Position(
                    points = persistentListOf(
                        Point(Srs.WGS84, 50.123456, -11.123456, name = "<script>alert()</script>"),
                    ),
                ).writeGpxPoints(this)
            }.toString(),
        )
    }

    @Test
    fun writeGpxPoints_noPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
</gpx>
""",
            StringBuilder().apply {
                Position().writeGpxPoints(this)
            }.toString(),
        )
    }
}
