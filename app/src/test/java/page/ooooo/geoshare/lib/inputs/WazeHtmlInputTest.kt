package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class WazeHtmlInputTest : InputTest {
    private val input = FakeInputRepository.wazeHtmlInput

    @Test
    fun parse_containsLatLngJSON_returnsPoint() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(43.64265563, -79.387202798, source = Source.JAVASCRIPT))),
            input.parse(
                """<html><script>
                |{
                |  "routing": {
                |    "to": {
                |      "address":"301 Front St W, Toronto, Ontario, Canada",
                |      "latLng":{"lat":43.64265563,"lng":-79.387202798},
                |      "title":"CN Tower"
                |    }
                |  },
                |  "nearbyVenues": [
                |    {
                |      "name": "Toronto Zoo",
                |      "latLng":{"lat":43.81809781005661,"lng":-79.18557484205378}
                |    }
                |  ]
                |}
                |</script></html>""".trimMargin()
            ),
        )
    }

    @Test
    fun parse_containsInvalidDataCoordinates_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult(),
            input.parse("""<html><script>{"routing": {"to": {"address":"301 Front St W, Toronto, Ontario, Canada","latLng":{"lat":spam,"lng":spam},"title":"CN Tower"}}}}</script></html>""")
        )
    }

    @Test
    fun parse_doesNotContainCoordinates_returnsNoPoints() = runTest {
        assertEquals(ParseResult(), input.parse("""<html></html>"""))
    }
}
