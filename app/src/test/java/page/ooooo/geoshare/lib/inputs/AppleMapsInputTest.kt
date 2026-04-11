package page.ooooo.geoshare.lib.inputs

import android.content.Context
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.ChinaGeometry
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
class AppleMapsInputTest : InputTest {
    private val mockContext: Context = mock {}
    private val chinaGeometry = ChinaGeometry(mockContext)
    private val coordinateConverter = CoordinateConverter(chinaGeometry)
    private val uriFormatter = UriFormatter(coordinateConverter)
    override val input = AppleMapsInput(uriFormatter)

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://maps.apple.com/?ll=50.894967,4.341626",
            getUri("https://maps.apple.com/?ll=50.894967,4.341626")
        )
        assertEquals("maps.apple.com/?ll=50.894967,4.341626", getUri("maps.apple.com/?ll=50.894967,4.341626"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals("https://maps.apple/p/7E-Brjrk_THN14", getUri("https://maps.apple/p/7E-Brjrk_THN14"))
        @Suppress("SpellCheckingInspection")
        assertEquals("maps.apple/p/7E-Brjrk_THN14", getUri("maps.apple/p/7E-Brjrk_THN14"))
    }

    @Test
    fun uriPattern_noPath() {
        assertEquals("https://maps.apple.com?q=foo", getUri("https://maps.apple.com?q=foo"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/?ll=50.894967,4.341626"))
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
        assertEquals(ParseUriResult(), parseUri("https://maps.apple.com"))
        assertEquals(ParseUriResult(), parseUri("https://maps.apple.com/"))
        assertEquals(ParseUriResult(), parseUri("https://maps.apple.com/?spam=1"))
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.URI))),
            parseUri("https://maps.apple.com/?ll=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        52.4890246, 13.4295963,
                        name = "Reuterplatz",
                        source = Source.URI,
                    )
                )
            ),
            parseUri("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902"),
        )
    }

    @Test
    fun parseUri_directionsCoordinates() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.URI))),
            parseUri("https://maps.apple.com/?daddr=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_directionsQuery() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI
                    )
                )
            ),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Test
    fun parseUri_view() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        52.49115540927951, 13.42595574770533,
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            parseUri("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"),
        )
    }

    @Test
    fun parseUri_searchQuery() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(name = "Central Park", source = Source.URI))),
            parseUri("https://maps.apple.com/?q=Central+Park"),
        )
    }

    @Test
    fun parseUri_searchLocation() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.MAP_CENTER))),
            parseUri("https://maps.apple.com/?sll=50.894967,4.341626"),
        )
    }

    @Test
    fun parseUri_searchLocationAndQueryAndZoom_returnsPointAndQueryAndZoom() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        50.894967, 4.341626,
                        name = "Central Park",
                        z = 10.0,
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            parseUri("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s"),
        )
    }

    @Test
    fun parseUri_searchLocationAndQueryAndInvalidZoom_returnsPointAndQuery() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        50.894967, 4.341626,
                        name = "Central Park",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            parseUri("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s"),
        )
    }

    @Test
    fun parseUri_parameterLlTakesPrecedenceOverCenterAndSllAndCoordinate() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            parseUri("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterNameTakesPrecedenceOverQAndAddressAndDaddr() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            parseUri("https://maps.apple.com/?name=Reuterplatz&q=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            parseUri("https://maps.apple.com/?name=Reuterplatz&address=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            parseUri("https://maps.apple.com/?name=Reuterplatz&daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterAddressTakesPrecedenceOverQAndDaddr() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            parseUri("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&daddr=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_parameterDaddrTakesPrecedenceOverQ() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            parseUri("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parseUri_auidOnly() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618",
            ),
            parseUri("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"),
        )
    }

    @Test
    fun parseUri_placeIdOnly() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902",
            ),
            parseUri("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"),
        )
    }

    @Test
    fun parseUri_placeIdAndQuery() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(WGS84Point(name = "Central Park", source = Source.URI)),
                htmlUriString = "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central%20Park",
            ),
            parseUri("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(),
                htmlUriString = "https://maps.apple/p/7E-Brjrk_THN14",
            ),
            parseUri("https://maps.apple/p/7E-Brjrk_THN14"),
        )
    }

    @Test
    fun parseHtml_success() = runTest {
        assertEquals(
            ParseHtmlResult(persistentListOf(WGS84Point(52.4735927, 13.4050798, source = Source.HTML))),
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
        assertEquals(ParseHtmlResult(), parseHtml("spam"))
    }
}
