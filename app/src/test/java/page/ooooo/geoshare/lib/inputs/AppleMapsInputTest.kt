package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

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
        assertEquals(
            "maps.apple.com/?ll=50.894967,4.341626",
            input.uriPattern.find("ftp://maps.apple.com/?ll=50.894967,4.341626")?.value,
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertTrue(parseUri("https://maps.apple.com") is ParseUriResult.Failed)
        assertTrue(parseUri("https://maps.apple.com/") is ParseUriResult.Failed)
        assertTrue(parseUri("https://maps.apple.com/?spam=1") is ParseUriResult.Failed)
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.894967, 4.341626))),
            parseUri("https://maps.apple.com/?ll=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.4890246, 13.4295963, name = "Reuterplatz"))),
            parseUri("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902"),
        )
    }

    @Test
    fun parseUri_directionsCoordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.894967, 4.341626))),
            parseUri("https://maps.apple.com/?daddr=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_directionsQuery() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz 3, 12047 Berlin, Germany"))),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Test
    fun parseUri_view() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(52.49115540927951, 13.42595574770533))),
            parseUri("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"),
        )
    }

    @Test
    fun parseUri_searchQuery() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Central Park"))),
            parseUri("https://maps.apple.com/?q=Central+Park"),
        )
    }

    @Test
    fun parseUri_searchLocation() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.894967, 4.341626))),
            parseUri("https://maps.apple.com/?sll=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_searchLocationAndQueryAndZoom_returnsPointAndQueryAndZoom() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(
                persistentListOf(WGS84Point(50.894967, 4.341626, name = "Central Park", z = 10.0))
            ),
            parseUri("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s"),
        )
    }

    @Test
    fun parseUri_searchLocationAndQueryAndInvalidZoom_returnsPointAndQuery() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(50.894967, 4.341626, name = "Central Park"))),
            parseUri("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s"),
        )
    }

    @Test
    fun parseUri_parameterLlTakesPrecedenceOverCenterAndSllAndCoordinate() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-17.2165721, -149.9470294))),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-17.2165721, -149.9470294))),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-17.2165721, -149.9470294))),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterNameTakesPrecedenceOverQAndAddressAndDaddr() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz"))),
            parseUri("https://maps.apple.com/?name=Reuterplatz&q=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz"))),
            parseUri("https://maps.apple.com/?name=Reuterplatz&address=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz"))),
            parseUri("https://maps.apple.com/?name=Reuterplatz&daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterAddressTakesPrecedenceOverQAndDaddr() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz 3, 12047 Berlin, Germany"))),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz 3, 12047 Berlin, Germany"))),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&daddr=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterDaddrTakesPrecedenceOverQ() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(name = "Reuterplatz 3, 12047 Berlin, Germany"))),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_auidOnly() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(),
                "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"
            ),
            parseUri("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"),
        )
    }

    @Test
    fun parseUri_placeIdOnly() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(),
                "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"
            ),
            parseUri("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"),
        )
    }

    @Test
    fun parseUri_placeIdAndQuery() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(WGS84Point(name = "Central Park")),
                "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central%20Park",
            ),
            parseUri("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(persistentListOf(), "https://maps.apple/p/7E-Brjrk_THN14"),
            parseUri("https://maps.apple/p/7E-Brjrk_THN14"),
        )
    }

    @Test
    fun parseHtml_success() = runTest {
        assertEquals(
            ParseHtmlResult.Succeeded(persistentListOf(WGS84Point(52.4735927, 13.4050798))),
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
        assertTrue(parseHtml("spam") is ParseHtmlResult.Failed)
    }
}
