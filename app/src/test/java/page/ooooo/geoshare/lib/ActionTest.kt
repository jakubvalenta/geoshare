package page.ooooo.geoshare.lib

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test

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
                            Point("50.123456", "-11.123456"),
                            Point("52.5067296", "13.2599309"),
                        ),
                    )
                ).write(this, uriQuote)
            }.toString()
        )
    }

    @Test
    fun toGpx_escapesSpecialCharacters() {
        assertEquals(
            """<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
<wpt lat="%22" lon="%3C" />
</gpx>
""",
            StringBuilder().apply {
                Action.SaveGpx(
                    Position(
                        points = persistentListOf(
                            Point("\"", "<"),
                        ),
                    )
                ).write(this, uriQuote)
            }.toString()
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
                    Position()
                ).write(this, uriQuote)
            }.toString()
        )
    }
}
