package page.ooooo.geoshare.lib

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.position.Point
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class ActionTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun saveGpx_write() {
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
                Action.SaveGpx(
                    Position(
                        points = persistentListOf(
                            Point(Srs.WGS84, 50.123456, -11.123456),
                            Point(Srs.WGS84, 52.5067296, 13.2599309),
                        ),
                    ),
                    uriQuote,
                ).write(this)
            }.toString(),
        )
    }

    @Test
    fun toGpx_escapesDesc() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="50.123456" lon="-11.123456">
    <desc>&lt;script&gt;alert()&lt;/script&gt;</desc>
</wpt>
</gpx>
""",
            StringBuilder().apply {
                Action.SaveGpx(
                    Position(
                        points = persistentListOf(
                            Point(Srs.WGS84, 50.123456, -11.123456, desc = "<script>alert()</script>"),
                        ),
                    ), uriQuote
                ).write(this)
            }.toString(),
        )
    }

    @Test
    fun toGpx_noPoints() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
</gpx>
""",
            StringBuilder().apply {
                Action.SaveGpx(
                    Position(),
                    uriQuote,
                ).write(this)
            }.toString(),
        )
    }
}
