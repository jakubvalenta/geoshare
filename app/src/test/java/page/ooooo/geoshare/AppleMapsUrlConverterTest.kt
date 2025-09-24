package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.AppleMapsUrlConverter

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
class AppleMapsUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = AppleMapsUrlConverter()

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://maps.apple.com/?ll=50.894967,4.341626"))
        assertTrue(doesUriPatternMatch("maps.apple.com/?ll=50.894967,4.341626"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://maps.apple/p/7E-Brjrk_THN14"))
        @Suppress("SpellCheckingInspection")
        assertTrue(doesUriPatternMatch("maps.apple/p/7E-Brjrk_THN14"))
    }

    @Test
    fun uriPattern_noPath() {
        assertTrue(doesUriPatternMatch("https://maps.apple.com?q=foo"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/?ll=50.894967,4.341626"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://maps.apple.com/?ll=50.894967,4.341626"))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://maps.apple.com"))
        assertNull(parseUrl("https://maps.apple.com/"))
        assertNull(parseUrl("https://maps.apple.com/?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("50.894967", "4.341626"),
            parseUrl("https://maps.apple.com/?ll=50.894967,4.341626")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position("52.4890246", "13.4295963"),
            parseUrl("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902")
        )
    }

    @Test
    fun parseUrl_view() {
        assertEquals(
            Position("52.49115540927951", "13.42595574770533"),
            parseUrl("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533")
        )
    }


    @Test
    fun parseUrl_searchQuery() {
        assertEquals(
            Position(lat = "0", lon = "0", q = "Central Park"),
            parseUrl("https://maps.apple.com/?q=Central+Park")
        )
    }

    @Test
    fun parseUrl_searchLocation() {
        assertEquals(
            Position("50.894967", "4.341626"),
            parseUrl("https://maps.apple.com/?sll=50.894967,4.341626")
        )
    }

    @Test
    fun parseUrl_searchLocationAndQuery() {
        assertEquals(
            Position("50.894967", "4.341626", q = "Central Park", z = "10"),
            parseUrl("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s")
        )
    }

    @Test
    fun parseUrl_searchLocationAndQueryWithInvalidZoom() {
        assertEquals(
            Position("50.894967", "4.341626", q = "Central Park"),
            parseUrl("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s")
        )
    }

    @Test
    fun parseUrl_parameterLlTakesPrecedence() {
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533")
        )
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&")
        )
        assertEquals(
            Position("-17.2165721", "-149.9470294"),
            parseUrl("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533")
        )
    }

    @Test
    fun parseUrl_parameterAddressTakesPrecedence() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz")
        )
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(q = "Reuterplatz 3, 12047 Berlin, Germany"),
            parseUrl("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&name=Reuterplatz")
        )
    }

    @Test
    fun parseUrl_parameterNameTakesPrecedenceOverQ() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Position(q = "Reuterplatz"),
            parseUrl("https://maps.apple.com/?name=Reuterplatz&q=Central%20Park")
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUrl_AuidOnly() {
        assertEquals(
            Position(),
            parseUrl("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618")
        )
    }

    @Test
    fun parseUrl_placeIdOnly() {
        assertEquals(
            Position(),
            parseUrl("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902")
        )
    }

    @Test
    fun parseUrl_placeIdAndQuery() {
        assertEquals(
            Position(),
            parseUrl("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park")
        )
    }

    @Test
    fun parseUrl_shortLink() {
        assertEquals(
            Position(),
            parseUrl("https://maps.apple/p/7E-Brjrk_THN14")
        )
    }

    @Test
    fun parseHtml_success() {
        assertEquals(
            Position("52.4735927", "13.4050798"),
            @Suppress("SpellCheckingInspection")
            parseHtml(
                """<html>
<head>
  <title>Tempelhofer Feld</title>
  <meta property="place:location:latitude" content="52.4735927" />
  <meta property="place:location:longitude" content="13.4050798" />
</head>
<body></body>
</html>
"""
            )
        )
    }

    @Test
    fun parseHtml_failure() {
        assertNull(parseHtml("spam"))
    }
}
