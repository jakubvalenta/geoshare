package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class WazeInputTest : BaseInputTest() {
    override val input = WazeInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://waze.com/ul?ll=45.6906304,-120.810983&z=10",
            getUri("https://waze.com/ul?ll=45.6906304,-120.810983&z=10")
        )
        assertEquals("waze.com/ul?ll=45.6906304,-120.810983&z=10", getUri("waze.com/ul?ll=45.6906304,-120.810983&z=10"))
        assertEquals(
            "https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983",
            getUri("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020",
            getUri("https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020",
            getUri("https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020",
            getUri("https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ",
            getUri("https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ")
        )
        assertEquals(
            "https://ul.waze.com/ul?venue_id=2884104.28644432.6709020&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location",
            getUri("https://ul.waze.com/ul?venue_id=2884104.28644432.6709020&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location")
        )
        assertEquals(
            "https://www.waze.com/ul?venue_id=2884104.28644432.6709020",
            getUri("https://www.waze.com/ul?venue_id=2884104.28644432.6709020")
        )
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("https://waze.com/ul/hu00uswvn3", getUri("https://waze.com/ul/hu00uswvn3"))
        @Suppress("SpellCheckingInspection") assertEquals("waze.com/ul/hu00uswvn3", getUri("waze.com/ul/hu00uswvn3"))
        assertEquals("https://www.waze.com/ul/hu00uswvn3", getUri("https://www.waze.com/ul/hu00uswvn3"))
        assertEquals("https://www.waze.com/live-map?h=u00uswvn3", getUri("https://www.waze.com/live-map?h=u00uswvn3"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/ul?ll=45.6906304,-120.810983&z=10"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "waze.com/ul?ll=45.6906304,-120.810983&z=10",
            input.uriPattern.find("ftp://waze.com/ul?ll=45.6906304,-120.810983&z=10")?.value,
        )
    }

    @Test
    fun uriPattern_replacement() {
        assertEquals(
            "https://waze.com/ul/hu00uswvn3",
            @Suppress("SpellCheckingInspection") getUri("Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3"),
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://waze.com"))
        assertEquals(ParseUriResult(), parseUri("https://waze.com/"))
        assertEquals(ParseUriResult(), parseUri("https://waze.com/ul"))
        assertEquals(ParseUriResult(), parseUri("https://waze.com/ul/?spam=1"))
        assertEquals(ParseUriResult(), parseUri("https://waze.com/live-map"))
        assertEquals(ParseUriResult(), parseUri("https://waze.com/live-map/?spam=1"))
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.6906304, -120.810983, z = 10.0))),
            parseUri("https://waze.com/ul?ll=45.6906304,-120.810983&z=10"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.69063040, -120.81098300))),
            parseUri("https://ul.waze.com/ul?ll=45.69063040%2C-120.81098300&navigate=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location"),
        )
    }

    @Test
    fun parseUri_directionsCoordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.6906304, -120.810983))),
            parseUri("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.829189, 1.259372))),
            parseUri("https://www.waze.com/live-map/directions?latlng=45.829189%2C1.259372"),
        )
    }

    @Test
    fun parseUri_directionsPlace() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020",
            ),
            parseUri("https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020",
            ),
            parseUri("https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020",
            ),
            parseUri("https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ"
            ),
            parseUri("https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020",
            ),
            parseUri("https://ul.waze.com/ul?venue_id=2884104.28644432.6709020&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020",
            ),
            parseUri("https://www.waze.com/ul?venue_id=2884104.28644432.6709020"),
        )
    }

    @Test
    fun parseUri_search() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(name = "66 Acacia Avenue"))),
            parseUri("https://waze.com/ul?q=66%20Acacia%20Avenue"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.829189, 1.259372, z = 16.0))),
            parseUri("https://waze.com/ul/hu00uswvn3"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.829189, 1.259372, z = 16.0))),
            parseUri("https://www.waze.com/ul/hu00uswvn3"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(45.829189, 1.259372, z = 16.0))),
            parseUri("https://www.waze.com/live-map?h=u00uswvn3"),
        )
    }

    @Test
    fun parseUri_shortLinkNegative() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(19.402564, -99.165666, z = 16.0))),
            parseUri("https://waze.com/ul/h9g3qrkju0"),
        )
    }

    @Test
    fun parseHtml_containsLatLngJSON_returnsPoint() = runTest {
        assertEquals(
            ParseHtmlResult(persistentListOf(WGS84Point(43.64265563, -79.387202798))),
            parseHtml(
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
    fun parseHtml_containsInvalidDataCoordinates_returnsNull() = runTest {
        assertEquals(
            ParseHtmlResult(),
            parseHtml("""<html><script>{"routing": {"to": {"address":"301 Front St W, Toronto, Ontario, Canada","latLng":{"lat":spam,"lng":spam},"title":"CN Tower"}}}}</script></html>""")
        )
    }

    @Test
    fun parseHtml_doesNotContainCoordinates_returnsNull() = runTest {
        assertEquals(ParseHtmlResult(), parseHtml("""<html></html>"""))
    }
}
