package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
class AppleMapsUriInputTest : InputTest {
    private val input = FakeInputRepository.appleMapsUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://maps.apple.com/?ll=50.894967,4.341626",
            input.match("https://maps.apple.com/?ll=50.894967,4.341626")
        )
        assertEquals("maps.apple.com/?ll=50.894967,4.341626", input.match("maps.apple.com/?ll=50.894967,4.341626"))
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://maps.apple/p/7E-Brjrk_THN14", input.match("https://maps.apple/p/7E-Brjrk_THN14"))
        @Suppress("SpellCheckingInspection")
        assertEquals("maps.apple/p/7E-Brjrk_THN14", input.match("maps.apple/p/7E-Brjrk_THN14"))
    }

    @Test
    fun match_noPath() {
        assertEquals("https://maps.apple.com?q=foo", input.match("https://maps.apple.com?q=foo"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/?ll=50.894967,4.341626"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "maps.apple.com/?ll=50.894967,4.341626",
            input.match("ftp://maps.apple.com/?ll=50.894967,4.341626"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://maps.apple.com/?q=foobar",
            input.match("https://maps.apple.com/?q=foobar ")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo bar",
            input.match("https://maps.apple.com/?q=foo bar ")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo",
            input.match("https://maps.apple.com/?q=foo  bar")
        )
        assertEquals(
            "https://maps.apple.com/?q=foo",
            input.match("https://maps.apple.com/?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://maps.apple.com"))
        assertEquals(ParseResult(), input.parse("https://maps.apple.com/"))
        assertEquals(ParseResult(), input.parse("https://maps.apple.com/?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=50.894967,4.341626"),
        )
    }

    @Test
    fun parse_place() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        52.4890246, 13.4295963,
                        name = "Reuterplatz",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/place?place-id=I1E40915DF4BA1C96&address=Reuterplatz+3,+12047+Berlin,+Germany&coordinate=52.4890246,13.4295963&name=Reuterplatz&_provider=9902"),
        )
    }

    @Test
    fun parse_directionsCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.URI))),
            input.parse("https://maps.apple.com/?daddr=50.894967,4.341626"),
        )
    }

    @Test
    fun parse_directionsQuery() = runTest {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI
                    )
                )
            ),
            input.parse("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Test
    fun parse_view() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        52.49115540927951, 13.42595574770533,
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"),
        )
    }

    @Test
    fun parse_searchQuery() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "Central Park", source = Source.URI))),
            input.parse("https://maps.apple.com/?q=Central+Park"),
        )
    }

    @Test
    fun parse_searchLocation() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.MAP_CENTER))),
            input.parse("https://maps.apple.com/?sll=50.894967,4.341626"),
        )
    }

    @Test
    fun parse_searchLocationAndQueryAndZoom_returnsPointAndQueryAndZoom() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        50.894967, 4.341626,
                        name = "Central Park",
                        z = 10.0,
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=10&t=s"),
        )
    }

    @Test
    fun parse_searchLocationAndQueryAndInvalidZoom_returnsPointAndQuery() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        50.894967, 4.341626,
                        name = "Central Park",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?q=Central+Park&sll=50.894967,4.341626&z=spam&t=s"),
        )
    }

    @Test
    fun parse_parameterLlTakesPrecedenceOverCenterAndSllAndCoordinate() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_parameterNameTakesPrecedenceOverQAndAddressAndDaddr() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            input.parse("https://maps.apple.com/?name=Reuterplatz&q=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            input.parse("https://maps.apple.com/?name=Reuterplatz&address=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            input.parse("https://maps.apple.com/?name=Reuterplatz&daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_parameterAddressTakesPrecedenceOverQAndDaddr() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&daddr=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_parameterDaddrTakesPrecedenceOverQ() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
    }

    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_auidOnly() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(),
                nextStep = NextStep(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"
                )
            ),
            input.parse("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"),
        )
    }

    @Test
    fun parse_placeIdOnly() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(),
                nextStep = NextStep(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"
                )
            ),
            input.parse("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"),
        )
    }

    @Test
    fun parse_placeIdAndQuery() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(WGS84Point(name = "Central Park", source = Source.URI)),
                nextStep = NextStep(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"
                )
            ),
            input.parse("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"),
        )
    }

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple/p/7E-Brjrk_THN14"
                )
            ),
            input.parse("https://maps.apple/p/7E-Brjrk_THN14"),
        )
    }
}
