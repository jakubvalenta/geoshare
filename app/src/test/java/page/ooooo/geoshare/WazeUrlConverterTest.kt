package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.WazeUrlConverter

class WazeUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = WazeUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://waze.com/ul?ll=45.6906304,-120.810983&z=10"))
        assertTrue(doesUriPatternMatch("waze.com/ul?ll=45.6906304,-120.810983&z=10"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/live-map/directions?place=w.183894452.1839010060.260192"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/live-map/directions?to=place.w.183894452.1839010060.260192"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.183894452.1839010060.260192"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ"))
        assertTrue(doesUriPatternMatch("https://ul.waze.com/ul?venue_id=183894452.1839010060.260192&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/ul?venue_id=183894452.1839010060.260192"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://waze.com/ul/hu00uswvn3"))
        @Suppress("SpellCheckingInspection")
        assertTrue(doesUriPatternMatch("waze.com/ul/hu00uswvn3"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/ul/hu00uswvn3"))
        assertTrue(doesUriPatternMatch("https://www.waze.com/live-map?h=u00uswvn3"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/ul?ll=45.6906304,-120.810983&z=10"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://waze.com/ul?ll=45.6906304,-120.810983&z=10"))
    }

    @Test
    fun uriPattern_replacement() {
        assertEquals(
            "https://waze.com/ul/hu00uswvn3",
            @Suppress("SpellCheckingInspection")
            getUri("Use Waze to drive to 5 - 22 Boulevard Gambetta: https://waze.com/ul/hu00uswvn3")
        )
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://waze.com"))
        assertNull(parseUrl("https://waze.com/"))
        assertNull(parseUrl("https://waze.com/ul"))
        assertNull(parseUrl("https://waze.com/ul/?spam=1"))
        assertNull(parseUrl("https://waze.com/live-map"))
        assertNull(parseUrl("https://waze.com/live-map/?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("45.6906304", "-120.810983", z = "10"),
            parseUrl("https://waze.com/ul?ll=45.6906304,-120.810983&z=10")
        )
        assertEquals(
            Position("45.69063040", "-120.81098300"),
            parseUrl("https://ul.waze.com/ul?ll=45.69063040%2C-120.81098300&navigate=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location")
        )
    }

    @Test
    fun parseUrl_directionsCoordinates() {
        assertEquals(
            Position("45.6906304", "-120.810983"),
            parseUrl("https://www.waze.com/live-map/directions?to=ll.45.6906304,-120.810983")
        )
        assertEquals(
            Position("45.829189", "1.259372"),
            parseUrl("https://www.waze.com/live-map/directions?latlng=45.829189%2C1.259372")
        )
    }

    @Test
    fun parseUrl_directionsPlace() {
        assertEquals(
            Position(),
            parseUrl("https://www.waze.com/live-map/directions?place=w.183894452.1839010060.260192")
        )
        assertEquals(
            Position(),
            parseUrl("https://www.waze.com/live-map/directions?to=place.w.183894452.1839010060.260192")
        )
        assertEquals(
            Position(),
            parseUrl("https://www.waze.com/live-map/directions/cn-tower-front-st-w-301-toronto?to=place.w.183894452.1839010060.260192")
        )
        assertEquals(
            Position(),
            parseUrl("https://www.waze.com/live-map/directions/potsdam-bb-de?to=place.ChIJt9Y6hM31qEcRm-yqC5j4ZcU&from=place.ChIJAVkDPzdOqEcRcDteW0YgIQQ")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position(),
            parseUrl("https://ul.waze.com/ul?venue_id=183894452.1839010060.260192&overview=yes&utm_campaign=default&utm_source=waze_website&utm_medium=lm_share_location")
        )
        assertEquals(
            Position(),
            parseUrl("https://www.waze.com/ul?venue_id=183894452.1839010060.260192")
        )
    }

    @Test
    fun parseUrl_search() {
        assertEquals(
            Position(q = "66 Acacia Avenue"),
            parseUrl("https://waze.com/ul?q=66%20Acacia%20Avenue")
        )
    }

    @Test
    fun isShortUrl_shortLink() {
        assertEquals(
            Position("45.829189", "1.259372"),
            parseUrl("https://waze.com/ul/hu00uswvn3")
        )
        assertEquals(
            Position("45.829189", "1.259372"),
            parseUrl("https://www.waze.com/ul/hu00uswvn3")
        )
        assertEquals(
            Position("45.829189", "1.259372"),
            parseUrl("https://www.waze.com/live-map?h=u00uswvn3")
        )
    }

    @Test
    fun isShortUrl_shortLinkNegative() {
        assertEquals(
            Position("19.402564", "-99.165666"),
            parseUrl("https://waze.com/ul/h9g3qrkju0")
        )
    }

    @Test
    fun parseHtml_containsLatLngJSON_returnsPosition() {
        assertEquals(
            Position("43.64265563", "-79.387202798"),
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
            )
        )
    }

    @Test
    fun parseHtml_containsInvalidDataCoordinates_returnsNull() {
        assertNull(
            parseHtml("""<html><script>{"routing": {"to": {"address":"301 Front St W, Toronto, Ontario, Canada","latLng":{"lat":spam,"lng":spam},"title":"CN Tower"}}}}</script></html>""")
        )
    }

    @Test
    fun parseHtml_doesNotContainCoordinates_returnsNull() {
        assertNull(
            parseHtml("""<html></html>""")
        )
    }
}
