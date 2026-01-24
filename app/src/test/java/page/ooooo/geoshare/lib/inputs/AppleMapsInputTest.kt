package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
class AppleMapsInputTest : BaseInputTest() {
    override val input = AppleMapsInput

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
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertNull(parseUri("https://maps.apple.com"))
        assertNull(parseUri("https://maps.apple.com/"))
        assertNull(parseUri("https://maps.apple.com/?spam=1"))
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 50.894967, 4.341626)),
            parseUri("https://maps.apple.com/?ll=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(Position(Srs.WGS84, 52.4890246, 13.4295963, name = "Reuterplatz")),
            parseUri("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902"),
        )
    }

    @Test
    fun parseUri_directionsCoordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 50.894967, 4.341626)),
            parseUri("https://maps.apple.com/?daddr=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_directionsQuery() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(Position(Srs.WGS84, q = "Reuterplatz 3, 12047 Berlin, Germany")),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Test
    fun parseUri_view() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 52.49115540927951, 13.42595574770533)),
            parseUri("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"),
        )
    }

    @Test
    fun parseUri_searchQuery() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, q = "Central Park")),
            parseUri("https://maps.apple.com/?q=Central+Park"),
        )
    }

    @Test
    fun parseUri_searchLocation() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 50.894967, 4.341626)),
            parseUri("https://maps.apple.com/?sll=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_searchLocationAndQueryAndZoom_returnsPointAndQueryAndZoom() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 50.894967, 4.341626, q = "Central Park", z = 10.0)),
            parseUri("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s"),
        )
    }

    @Test
    fun parseUri_searchLocationAndQueryAndInvalidZoom_returnsPointAndQuery() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, 50.894967, 4.341626, q = "Central Park")),
            parseUri("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s"),
        )
    }

    @Test
    fun parseUri_parameterLlTakesPrecedenceOverCenterAndSllAndCoordinate() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -17.2165721, -149.9470294)),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"),
        )
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -17.2165721, -149.9470294)),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"),
        )
        assertEquals(
            ParseUriResult.Succeeded(Position(Srs.WGS84, -17.2165721, -149.9470294)),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterAddressTakesPrecedenceOverQAndNameAndDaddr() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(q = "Reuterplatz 3, 12047 Berlin, Germany")),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
        assertEquals(
            ParseUriResult.Succeeded(Position(q = "Reuterplatz 3, 12047 Berlin, Germany")),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&name=Reuterplatz"),
        )
        assertEquals(
            ParseUriResult.Succeeded(Position(q = "Reuterplatz 3, 12047 Berlin, Germany")),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&daddr=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterDaddrTakesPrecedenceOverQAndName() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(q = "Reuterplatz 3, 12047 Berlin, Germany")),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
        assertEquals(
            ParseUriResult.Succeeded(Position(q = "Reuterplatz 3, 12047 Berlin, Germany")),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&name=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterNameTakesPrecedenceOverQ() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(Position(q = "Reuterplatz")),
            parseUri("https://maps.apple.com/?name=Reuterplatz&q=Central%20Park"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_auidOnly() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(),
                "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"
            ),
            parseUri("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"),
        )
    }

    @Test
    fun parseUri_placeIdOnly() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(),
                "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"
            ),
            parseUri("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"),
        )
    }

    @Test
    fun parseUri_placeIdAndQuery() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                Position(q = "Central Park"),
                "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central%20Park",
            ),
            parseUri("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(Position(), "https://maps.apple/p/7E-Brjrk_THN14"),
            parseUri("https://maps.apple/p/7E-Brjrk_THN14"),
        )
    }

    @Test
    fun parseHtml_success() = runTest {
        assertEquals(
            ParseHtmlResult.Succeeded(Position(Srs.WGS84, 52.4735927, 13.4050798)),
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
            ),
        )
    }

    @Test
    fun parseHtml_failure() = runTest {
        assertNull(parseHtml("spam"))
    }
}
