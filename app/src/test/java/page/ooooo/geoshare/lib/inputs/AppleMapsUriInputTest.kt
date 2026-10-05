package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

/**
 * See https://developer.apple.com/library/archive/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html
 */
class AppleMapsUriInputTest : InputTest {
    override val resources: Resources = mock()
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
        assertEquals(ParseResult.Success(), input.parse("https://maps.apple.com"))
        assertEquals(ParseResult.Success(), input.parse("https://maps.apple.com/"))
        assertEquals(ParseResult.Success(), input.parse("https://maps.apple.com/?spam=1"))
    }

    @Test
    fun parse_coordinates() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        52.345329, 4.940180,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "De Nieuwe Ooster",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/place?address=De%20Nieuwe%20Ooster%2C%20Kruislaan%20128%2C%201097%20GA%20Amsterdam%2C%20Netherlands&coordinate=52.345329%2C4.940180&name=De%20Nieuwe%20Ooster"),
        )
    }

    @Test
    fun parse_coordinatesLL() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=50.894967,4.341626"),
        )
    }

    @Test
    fun parse_placeWithCoordinate() = runTest {
        assertEquals(
            @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
            ParseResult.Success(
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

    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
    @Test
    fun parse_placeWithAuidOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(),
                next = MatchedInput(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"
                )
            ),
            input.parse("https://maps.apple.com/place?auid=17017496253231963769&lsp=7618"),
        )
    }

    @Test
    fun parse_placeWithPlaceIdOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(),
                next = MatchedInput(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"
                )
            ),
            input.parse("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902"),
        )
    }

    @Test
    fun parse_placeWithPlaceIdAndQuery() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(WGS84Point(name = "Central Park", source = Source.URI)),
                next = MatchedInput(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"
                )
            ),
            input.parse("https://maps.apple.com/place?place-id=I3B04EDEB21D5F86&_provider=9902&q=Central+Park"),
        )
    }

    @Test
    fun parse_directionsCoordinateDestinationOnly() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.URI))),
            input.parse("https://maps.apple.com/?daddr=50.894967,4.341626"),
        )
    }

    @Test
    fun parse_directionsAddressOriginAndAddressDestination() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = "Amsterdam Science Park, Amsterdam Netherlands",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Kruislaan 128, Kruislaan 128 1097 GA Amsterdam Netherlands",
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://maps.apple.com/directions?source=Amsterdam%20Science%20Park%2C%20Amsterdam%20Netherlands&source-place-id=IF27D33E7AAEB0155&destination=Kruislaan%20128%2C%20Kruislaan%20128%201097%20GA%20Amsterdam%20Netherlands&mode=walking"),
        )
    }

    @Test
    fun parse_directionsAddressDestinationOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Test
    fun parse_directionsWithCoordinateWaypoints() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Hugo de Vrieslaan, Hugo de Vrieslaan Amsterdam Netherlands",
                        source = Source.URI,
                    ),
                    WGS84Point(52.352443, 4.921501, source = Source.URI),
                    WGS84Point(52.355175, 4.928403, source = Source.URI),
                    WGS84Point(52.351108, 4.935267, source = Source.URI),
                )
            ),
            input.parse("https://maps.apple.com/directions?source=Hugo%20de%20Vrieslaan%2C%20Hugo%20de%20Vrieslaan%20Amsterdam%20Netherlands&waypoint=52.352443%2C4.921501&waypoint=52.355175%2C4.928403&destination=52.351108%2C4.935267&mode=driving"),
        )
    }

    @Test
    fun parse_directionsWithNameWaypoints() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(52.350284, 4.944259, source = Source.URI),
                    WGS84Point(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                        "Middenweg 331, Middenweg 331, 1098 AT Amsterdam, Netherlands",
                        source = Source.URI,
                    ),
                    WGS84Point(52.346449, 4.950065, source = Source.URI),
                )
            ),
            input.parse("https://maps.apple.com/directions?source=52.350284%2C4.944259&waypoint=Middenweg%20331%2C%20Middenweg%20331%2C%201098%20AT%20Amsterdam%2C%20Netherlands&destination=52.346449%2C4.950065&mode=driving"),
        )
    }

    @Test
    fun parse_mapCenter() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(52.49115540927951, 13.42595574770533, source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://maps.apple.com/search?span=0.0076562252877820924,0.009183883666992188&center=52.49115540927951,13.42595574770533"),
        )
    }

    @Test
    fun parse_apiCoordinateDirections() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(52.352443, 4.921501, source = Source.URI),
                    WGS84Point(52.355175, 4.928403, source = Source.URI),
                )
            ),
            input.parse("http://maps.apple.com/?saddr=52.352443%2C4.921501&daddr=52.355175%204.928403"),
        )
    }

    @Test
    fun parse_apiNameDirections() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(name = "San Jose", source = Source.URI),
                    WGS84Point(name = "San Francisco", source = Source.URI),
                )
            ),
            input.parse("http://maps.apple.com/?saddr=San+Jose&daddr=San+Francisco&dirflg=r"),
        )
    }

    @Test
    fun parse_searchAddress() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = "1,Infinite Loop,Cupertino,California",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("http://maps.apple.com/?address=1,Infinite+Loop,Cupertino,California"),
        )
    }

    @Test
    fun parse_searchQuery() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(name = "Central Park", source = Source.URI))),
            input.parse("https://maps.apple.com/?q=Central+Park"),
        )
    }

    @Test
    fun parse_searchCenter() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.MAP_CENTER))),
            input.parse("https://maps.apple.com/?sll=50.894967,4.341626"),
        )
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(50.894967, 4.341626, source = Source.MAP_CENTER))),
            input.parse("https://maps.apple.com/?near=50.894967,4.341626"),
        )
    }

    @Test
    fun parse_searchQueryAndCenterAndZoom_returnsPointWithNameAndZoom() = runTest {
        assertEquals(
            ParseResult.Success(
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
    fun parse_searchQueryAndCenterAndInvalidZoom_returnsPointWithName() = runTest {
        assertEquals(
            ParseResult.Success(
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
    fun parse_parameterLLTakesPrecedence() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&center=52.49115540927951,13.42595574770533"),
        )
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&sll=52.49115540927951,13.42595574770533&"),
        )
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&coordinate=52.49115540927951,13.42595574770533"),
        )
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(-17.2165721, -149.9470294, source = Source.URI))),
            input.parse("https://maps.apple.com/?ll=-17.2165721,-149.9470294&&near=52.49115540927951,13.42595574770533"),
        )
    }

    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
    @Test
    fun parse_parameterNameTakesPrecedence() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            input.parse("https://maps.apple.com/?name=Reuterplatz&q=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(name = "Reuterplatz", source = Source.URI))),
            input.parse("https://maps.apple.com/?name=Reuterplatz&address=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
    }

    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
    @Test
    fun parse_parameterAddressTakesPrecedence() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&q=Reuterplatz"),
        )
    }

    @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
    @Test
    fun parse_parameterDaddrTakesPrecedence() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz 3, 12047 Berlin, Germany",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?name=Reuterplatz&daddr=Reuterplatz+3,+12047+Berlin,+Germany"),
        )
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = "Reuterplatz",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://maps.apple.com/?address=Reuterplatz+3,+12047+Berlin,+Germany&daddr=Reuterplatz"),
        )
        assertEquals(
            ParseResult.Success(
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

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult.Success(
                next = MatchedInput(
                    FakeInputRepository.appleMapsHtmlInput,
                    "https://maps.apple/p/7E-Brjrk_THN14"
                )
            ),
            input.parse("https://maps.apple/p/7E-Brjrk_THN14"),
        )
    }
}
