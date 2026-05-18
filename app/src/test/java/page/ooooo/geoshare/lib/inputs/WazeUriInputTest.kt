package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class WazeUriInputTest : InputTest {
    private val input = WazeUriInput(WazeHtmlInput())

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://waze.com/ul?ll=45.6906304,-120.810983&z=10",
            input.match("https://waze.com/ul?ll=45.6906304,-120.810983&z=10")
        )
        assertEquals(
            "waze.com/ul?ll=45.6906304,-120.810983&z=10",
            input.match("waze.com/ul?ll=45.6906304,-120.810983&z=10")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983",
            input.match("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020",
            input.match("https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020",
            input.match("https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020",
            input.match("https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020")
        )
        assertEquals(
            "https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ",
            input.match("https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ")
        )
        assertEquals(
            "https://ul.waze.com/ul?venue_id=2884104.28644432.6709020&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location",
            input.match("https://ul.waze.com/ul?venue_id=2884104.28644432.6709020&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location")
        )
        assertEquals(
            "https://www.waze.com/ul?venue_id=2884104.28644432.6709020",
            input.match("https://www.waze.com/ul?venue_id=2884104.28644432.6709020")
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://waze.com/ul/hu00uswvn3", input.match("https://waze.com/ul/hu00uswvn3"))
        @Suppress("SpellCheckingInspection") assertEquals(
            "waze.com/ul/hu00uswvn3",
            input.match("waze.com/ul/hu00uswvn3")
        )
        assertEquals("https://www.waze.com/ul/hu00uswvn3", input.match("https://www.waze.com/ul/hu00uswvn3"))
        assertEquals(
            "https://www.waze.com/live-map?h=u00uswvn3",
            input.match("https://www.waze.com/live-map?h=u00uswvn3")
        )
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/ul?ll=45.6906304,-120.810983&z=10"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "waze.com/ul?ll=45.6906304,-120.810983&z=10",
            input.match("ftp://waze.com/ul?ll=45.6906304,-120.810983&z=10"),
        )
    }

    @Test
    fun match_replacement() {
        assertEquals(
            "https://waze.com/ul/hu00uswvn3",
            @Suppress("SpellCheckingInspection") input.match("Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://waze.com/?q=foobar",
            input.match("https://waze.com/?q=foobar ")
        )
        assertEquals(
            "https://waze.com/?q=foo bar",
            input.match("https://waze.com/?q=foo bar ")
        )
        assertEquals(
            "https://waze.com/?q=foo",
            input.match("https://waze.com/?q=foo  bar")
        )
        assertEquals(
            "https://waze.com/?q=foo",
            input.match("https://waze.com/?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://waze.com"))
        assertEquals(ParseResult(), input.parse("https://waze.com/"))
        assertEquals(ParseResult(), input.parse("https://waze.com/ul"))
        assertEquals(ParseResult(), input.parse("https://waze.com/ul/?spam=1"))
        assertEquals(ParseResult(), input.parse("https://waze.com/live-map"))
        assertEquals(ParseResult(), input.parse("https://waze.com/live-map/?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.6906304, -120.810983, z = 10.0, source = Source.URI))),
            input.parse("https://waze.com/ul?ll=45.6906304,-120.810983&z=10"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.69063040, -120.81098300, source = Source.URI))),
            input.parse("https://ul.waze.com/ul?ll=45.69063040%2C-120.81098300&navigate=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location"),
        )
    }

    @Test
    fun parse_directionsCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.6906304, -120.810983, source = Source.URI))),
            input.parse("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.829189, 1.259372, source = Source.URI))),
            input.parse("https://www.waze.com/live-map/directions?latlng=45.829189%2C1.259372"),
        )
    }

    @Test
    fun parse_directionsPlace() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    WazeHtmlInput(),
                    "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020"
                )
            ),
            input.parse("https://www.waze.com/live-map/directions?place=w.2884104.28644432.6709020"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    WazeHtmlInput(),
                    "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020"
                )
            ),
            input.parse("https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    WazeHtmlInput(),
                    "https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020"
                )
            ),
            input.parse("https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.2884104.28644432.6709020"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    WazeHtmlInput(),
                    "https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ"
                )
            ),
            input.parse("https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ"),
        )
    }

    @Test
    fun parse_place() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    WazeHtmlInput(),
                    "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020"
                )
            ),
            input.parse("https://ul.waze.com/ul?venue_id=2884104.28644432.6709020&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    WazeHtmlInput(),
                    "https://www.waze.com/live-map/directions?to=place.w.2884104.28644432.6709020"
                )
            ),
            input.parse("https://www.waze.com/ul?venue_id=2884104.28644432.6709020"),
        )
    }

    @Test
    fun parse_search() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "66 Acacia Avenue", source = Source.URI))),
            input.parse("https://waze.com/ul?q=66%20Acacia%20Avenue"),
        )
    }

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.829189, 1.259372, z = 16.0, source = Source.HASH))),
            input.parse("https://waze.com/ul/hu00uswvn3"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.829189, 1.259372, z = 16.0, source = Source.HASH))),
            input.parse("https://www.waze.com/ul/hu00uswvn3"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(45.829189, 1.259372, z = 16.0, source = Source.HASH))),
            input.parse("https://www.waze.com/live-map?h=u00uswvn3"),
        )
    }

    @Test
    fun parse_shortLinkNegative() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(19.402564, -99.165666, z = 16.0, source = Source.HASH))),
            input.parse("https://waze.com/ul/h9g3qrkju0"),
        )
    }
}
