package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class GoogleMapsUriInputTest : InputTest {
    private val input = FakeInputRepository.googleMapsUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://www.google.com/maps/@52.5067296,13.2599309,6z",
            input.match("https://www.google.com/maps/@52.5067296,13.2599309,6z")
        )
        assertEquals(
            "google.com/maps/@52.5067296,13.2599309,6z",
            input.match("google.com/maps/@52.5067296,13.2599309,6z")
        )
    }

    @Test
    fun match_urlWithSpace() {
        assertEquals(
            "https://maps.google.com/maps?f=d&daddr=2088 Albion Rd+@43.7481,-79.6332",
            input.match("https://maps.google.com/maps?f=d&daddr=2088 Albion Rd+@43.7481,-79.6332")
        )
    }

    @Test
    fun match_noPath() {
        assertEquals("https://maps.google.com?q=foo", input.match("https://maps.google.com?q=foo"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/maps/@52.5067296,13.2599309,6z"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "www.google.com/maps/@52.5067296,13.2599309,6z",
            input.match("ftp://www.google.com/maps/@52.5067296,13.2599309,6z"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://maps.google.com/?q=foobar",
            input.match("https://maps.google.com/?q=foobar ")
        )
        assertEquals(
            "https://maps.google.com/?q=foo bar",
            input.match("https://maps.google.com/?q=foo bar ")
        )
        assertEquals(
            "https://maps.google.com/?q=foo",
            input.match("https://maps.google.com/?q=foo  bar")
        )
        assertEquals(
            "https://maps.google.com/?q=foo",
            input.match("https://maps.google.com/?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(
            ParseResult(),
            input.parse("https://maps.google.com"),
        )
        assertEquals(
            ParseResult(),
            input.parse("https://maps.google.com/"),
        )
        assertEquals(
            ParseResult(),
            input.parse("https://maps.google.com/?spam=1"),
        )
    }

    @Test
    fun parse_unknownPath() = runTest {
        assertEquals(ParseResult(), input.parse("https://maps.google.com/spam"))
    }

    @Test
    fun parse_coordinatesOnly() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(52.5067296, 13.2599309, z = 6.0, source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://www.google.com/maps/@52.5067296,13.2599309,6z"),
        )
    }

    @Test
    fun parse_coordinatesOnlyStreetView() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(53.512825, 57.6891441, source = Source.MAP_CENTER)
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/@53.512825,57.6891441,0a,75y,90t/data=abc?utm_source=mstt_0&g_ep=def"),
        )
    }

    @Test
    fun parse_placeAndPositiveCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.5067296, 13.2599309,
                        z = 11.0,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }

    @Test
    fun parse_placeAndPositiveCoordinatesWithManyDecimalPlaces() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        44.448337599999995, 26.0834555,
                        name = @Suppress("SpellCheckingInspection") "Strada Occidentului 7, București, Romania",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://www.google.com/maps/place/Strada+Occidentului+7,+Bucure%C8%99ti,+Romania/data=!4m6!3m5!1s0x40b201fdfa573623:0x4f53bb5ad3fdc97f!7e2!8m2!3d44.448337599999995!4d26.0834555?utm_source=mstt_1&entry=gps&coh=192189&g_ep=abc"),
        )
    }

    @Test
    fun parse_placeAndNegativeCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        -17.2165721, -149.9470294,
                        z = 11.0,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"),
        )
    }

    @Test
    fun parse_placeAndIntegerCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.0, 13.0,
                        z = 11.0,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER
                    )
                )
            ),
            input.parse("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"),
        )
    }

    @Test
    fun parse_placeAndFractionalZoom() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.5067296, 13.2599309,
                        z = 6.33,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"),
        )
    }

    @Test
    fun parse_placeAndData() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        40.785091, -73.968285,
                        z = 15.0,
                        name = "Central Park",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2"),
        )
    }

    @Test
    fun parse_placeAndPositiveCoordinatesAndPositiveDataCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        44.4490541, 26.0888398,
                        z = 11.42,
                        name = "RAI - Romantic & Intimate",
                        source = Source.URI,
                    )
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate/@44.5190589,25.7489796,11.42z/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs"),
        )
    }

    @Test
    fun parse_placeAndNegativeCoordinatesAndNegativeDataCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(40.785091, -73.968285, z = 15.0, name = "Central Park", source = Source.URI)
                )
            ),
            input.parse("https://www.google.com/maps/place/Central+Park/@40.8,-73.9,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285"),
        )
    }

    @Test
    fun parse_placeAndPositiveDataCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        44.4490541, 26.0888398,
                        name = @Suppress("SpellCheckingInspection") "RAI - Romantic & Intimate, Calea Victoriei 202 București, Bucuresti 010098, România",
                        source = Source.URI,
                    )
                ),
            ),
            input.parse("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate,+Calea+Victoriei+202+Bucure%C8%99ti,+Bucuresti+010098,+Rom%C3%A2nia/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs"),
        )
    }

    @Test
    fun parse_placeAsCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(52.04, -2.35, z = 15.0, source = Source.URI))
            ),
            input.parse("https://maps.google.com/maps/place/52.04,-2.35/@52.03877,-2.3416,15z/data=!3m1!1e3"),
        )
    }

    @Test
    fun parse_placeAsCoordinatesWithPlus() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(52.492611, 13.431726, z = 17.0, source = Source.URI))
            ),
            input.parse("https://www.google.com/maps/place/52.492611,+13.431726/@52.4929475,13.4317905,17z/data=!4m4!3m3!8m2?force=pwa"),
        )
    }

    @Test
    fun parse_placeCoordinatesOnly() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(52.03877, -2.3416, source = Source.URI))
            ),
            input.parse("https://maps.google.com/maps/place/52.03877,-2.3416/data=!3m1!1e3"),
        )
    }

    @Test
    fun parse_placeOnly() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        name = "Poznań Old Town, 61-001 Poznań, Poland",
                        source = Source.URI,
                    )
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"
                )
            ),
            input.parse("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        name = @Suppress("SpellCheckingInspection") "Wikimedia Foundation, Inc., 1 Sansome St #1895, San Francisco, CA 94104, Vereinigte Staaten",
                        source = Source.URI,
                    )
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/place/Wikimedia+Foundation,+Inc.,+1+Sansome+St+%231895,+San+Francisco,+CA+94104,+Vereinigte+Staaten/data=!4m2!3m1!1s0x8085807d3bb6272b:0xfeadb8d7203f8179!17m2!4m1!1e3!18m1!1e1"
                )
            ),
            input.parse("https://www.google.com/maps/place/Wikimedia+Foundation,+Inc.,+1+Sansome+St+%231895,+San+Francisco,+CA+94104,+Vereinigte+Staaten/data=!4m2!3m1!1s0x8085807d3bb6272b:0xfeadb8d7203f8179!17m2!4m1!1e3!18m1!1e1"),
        )
    }

    @Test
    fun parse_placeWithoutName() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda"
                )
            ),
            input.parse("https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda"),
        )
    }

    @Test
    fun parse_placeList() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsPlaceListInput,
                    "https://www.google.com/maps/placelists/list/XXX?g_ep=ghijkl%3D&g_st=isi"
                )
            ),
            input.parse("https://www.google.com/maps/placelists/list/XXX?g_ep=ghijkl%3D&g_st=isi"),
        )
    }

    @Test
    fun parse_placeListInData() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsPlaceListInput,
                    "https://www.google.com/maps/@/data=!3m1!4b1!4m3!11m2!2sXXX!3e3?skid=foo&g_ep=bar&entry=tts"
                )
            ),
            input.parse("https://www.google.com/maps/@/data=!3m1!4b1!4m3!11m2!2sXXX!3e3?skid=foo&g_ep=bar&entry=tts"),
        )
    }

    @Test
    fun parse_searchCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(48.8584, 2.2945, source = Source.URI))
            ),
            input.parse("https://www.google.com/maps/search/48.8584,2.2945"),
        )
    }

    @Test
    fun parse_searchPlace() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "restaurants near me", source = Source.URI)),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/search/restaurants+near+me"
                )
            ),
            input.parse("https://www.google.com/maps/search/restaurants+near+me"),
        )
    }

    @Test
    fun parse_searchQueryCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(47.5951518, -122.3316393, source = Source.URI))
            ),
            input.parse("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518,-122.3316393&api=1"),
        )
    }

    @Test
    fun parse_searchQueryPlace() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        name = @Suppress("SpellCheckingInspection") "centurylink+field",
                        source = Source.URI,
                    )
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield"
                )
            ),
            input.parse("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield"),
        )
    }

    @Test
    fun parse_searchQueryPlaceWithPlaceId() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "Lumen Field", source = Source.URI)),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsPlaceApiInput,
                    "https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=Lumen%20Field&api=1",
                )
            ),
            input.parse("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=Lumen%20Field&api=1"),
        )
    }

    @Test
    fun parse_searchQueryPlaceIdOnly() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsPlaceApiInput,
                    "https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&api=1",
                )
            ),
            input.parse("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&api=1"),
        )
    }

    @Test
    fun parse_parameterLLTakesPrecedenceOverViewpointAndCenter() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(49.93556240, -7.30123395, source = Source.URI))
            ),
            input.parse("https://maps.google.com/?ll=49.93556240,-7.30123395&viewpoint=34.0522,-118.2437"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(49.93556240, -7.30123395, source = Source.URI))
            ),
            input.parse("https://maps.google.com/?ll=49.93556240,-7.30123395&center=34.0522,-118.2437"),
        )
    }

    @Test
    fun parse_parameterQTakesPrecedenceOverLLAndViewpointAndCenter() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(40.7128, -74.0060, source = Source.URI))
            ),
            input.parse("https://www.google.com/?q=40.7128,-74.0060&ll=34.0522,-118.2437"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(40.7128, -74.0060, source = Source.URI))
            ),
            input.parse("https://www.google.com/?q=40.7128,-74.0060&viewpoint=34.0522,-118.2437"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(40.7128, -74.0060, source = Source.URI))
            ),
            input.parse("https://www.google.com/?q=40.7128,-74.0060&center=34.0522,-118.2437"),
        )
    }

    @Test
    fun parse_parameterDestinationTakesPrecedence() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "Cherbourg,France", source = Source.URI)),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/?destination=Cherbourg,France&q=Paris,France"
                )
            ),
            input.parse("https://www.google.com/?destination=Cherbourg,France&q=Paris,France"),
        )
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "Cherbourg,France", source = Source.URI)),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/?destination=Cherbourg,France&query=Paris,France"
                )
            ),
            input.parse("https://www.google.com/?destination=Cherbourg,France&query=Paris,France"),
        )
    }

    @Test
    fun parse_directionsCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(40.7128, -74.0060, source = Source.URI),
                    GCJ02MainlandChinaPoint(34.0522, -118.2437, source = Source.URI),
                )
            ),
            input.parse("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437"),
        )
    }

    @Test
    fun parse_directionsCoordinatesWithCenter() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(40.7128, -74.0060, source = Source.URI),
                    GCJ02MainlandChinaPoint(34.0522, -118.2437, z = 16.0, source = Source.URI),
                )
            ),
            input.parse("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437/@52.4844406,13.4217121,16z/"),
        )
    }

    @Test
    fun parse_directionsPlaceAndCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(name = "My location", source = Source.URI),
                    GCJ02MainlandChinaPoint(
                        48.83887481689453, 2.2740750312805176,
                        z = 8.0,
                        name = @Suppress("SpellCheckingInspection") "Hôpital Européen Georges Pompidou Assistance Publique-Hôpitaux de Paris,20 r Leblanc, 75015 Paris",
                        source = Source.MAP_CENTER,
                    ),
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir/My+location/H%c3%b4pital+Europ%c3%a9en+Georges+Pompidou+Assistance+Publique-H%c3%b4pitaux+de+Paris,20+r+Leblanc%2c+75015+Paris/@48.83887481689453,2.2740750312805176,8z/"),
        )
    }

    @Test
    fun parse_directionsFromTo() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(name = "New York, NY", source = Source.URI),
                    GCJ02MainlandChinaPoint(name = "Los Angeles, CA", source = Source.URI),
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA"
                )
            ),
            input.parse("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA"),
        )
    }

    @Test
    fun parse_directionsFromToWithInvalidData() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(name = "Berlin", source = Source.URI),
                    GCJ02MainlandChinaPoint(name = "Potsdam", source = Source.URI),
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/dir/Berlin/Potsdam/data=spam"
                )
            ),
            input.parse("https://www.google.com/maps/dir/Berlin/Potsdam/data=spam"),
        )
    }

    @Test
    fun parse_directionsFromToVia() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(name = "New York, NY", source = Source.URI),
                    GCJ02MainlandChinaPoint(name = "Philadelphia, PA", source = Source.URI),
                    GCJ02MainlandChinaPoint(name = "Washington, DC", source = Source.URI),
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC"
                )
            ),
            input.parse("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC"),
        )
    }

    @Test
    fun parse_directionsFromToViaWithCenter() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        name = @Suppress("SpellCheckingInspection") "Hermannstraße 1, 12049 Berlin, Germany",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        name = @Suppress("SpellCheckingInspection") "Weserstr. 1, 12047 Berlin, Germany",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        52.4844406, 13.4217121,
                        z = 16.0,
                        name = @Suppress("SpellCheckingInspection") "Reuterstraße 1, Berlin-Neukölln, Germany",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/"),
        )
    }

    @Test
    fun parse_directionsFromToViaWithCoordinatesInData() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.4858222, 13.4236883,
                        name = @Suppress("SpellCheckingInspection") "Hermannstraße 1, 12049 Berlin, Germany",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        52.4881038, 13.4255518,
                        name = @Suppress("SpellCheckingInspection") "Weserstr. 1, 12047 Berlin, Germany",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        52.4807739, 13.4300356,
                        name = @Suppress("SpellCheckingInspection") "Reuterstraße 1, Berlin-Neukölln, Germany",
                        z = 16.0,
                        source = Source.URI,
                    ),
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir/Hermannstra%C3%9Fe+1,+12049+Berlin,+Germany/Weserstr.+1,+12047+Berlin,+Germany/Reuterstra%C3%9Fe+1,+Berlin-Neuk%C3%B6lln,+Germany/@52.4844406,13.4217121,16z/data=!3m1!4b1!4m20!4m19!1m5!1m1!1s0x47a84fb831937021:0x28d6914e5ca0f9f5!2m2!1d13.4236883!2d52.4858222!1m5!1m1!1s0x47a84fb7098f1d89:0x74c8a84ad2981e9f!2m2!1d13.4255518!2d52.4881038!1m5!1m1!1s0x47a84fbb7c0791d7:0xf6e39aaedab8b2d9!2m2!1d13.4300356!2d52.4807739!3e2"),
        )
    }

    @Test
    fun parse_directionsWithSeveralWaypoints() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        55.626402299999995, 37.1331874,
                        name = "Ликино",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        55.637071, 37.206128,
                        name = "Лесной Городок",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        55.6826036, 37.3149893,
                        name = "Ба́ковка",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        55.7073371, 37.3859881,
                        name = "АШАН",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        55.658595899999995, 37.4428281,
                        name = "Востряковское кладбище",
                        source = Source.URI,
                    ),
                    GCJ02MainlandChinaPoint(
                        55.6116874, 37.686148599999996,
                        name = "Музей-заповедник Царицыно",
                        source = Source.URI,
                    ),
                )
            ),
            input.parse(
                "https://www.google.com/maps/dir/%D0%9B%D0%B8%D0%BA%D0%B8%D0%BD%D0%BE/%D0%9B%D0%B5%D1%81%D0%BD%D0%BE%D0%B9+%D0%93%D0%BE%D1%80%D0%BE%D0%B4%D0%BE%D0%BA/%D0%91%D0%B0%CC%81%D0%BA%D0%BE%D0%B2%D0%BA%D0%B0/%D0%90%D0%A8%D0%90%D0%9D/%D0%92%D0%BE%D1%81%D1%82%D1%80%D1%8F%D0%BA%D0%BE%D0%B2%D1%81%D0%BA%D0%BE%D0%B5+%D0%BA%D0%BB%D0%B0%D0%B4%D0%B1%D0%B8%D1%89%D0%B5/%D0%9C%D1%83%D0%B7%D0%B5%D0%B9-%D0%B7%D0%B0%D0%BF%D0%BE%D0%B2%D0%B5%D0%B4%D0%BD%D0%B8%D0%BA+%D0%A6%D0%B0%D1%80%D0%B8%D1%86%D1%8B%D0%BD%D0%BE/data=!4m38!4m37!1m5!1m4!1s0x46b55767341de071:0xe1281fa8dd2b9f9c!8m2!3d55.626402299999995!4d37.1331874!1m5!1m4!1s0x46b550cf63985f73:0x6b36a6abc6d503bc!8m2!3d55.637071!4d37.206128!1m5!1m4!1s0x46b551d28d217481:0x798f7f7ed913cf5!8m2!3d55.6826036!4d37.3149893!1m5!1m4!1s0x46b55031416094cd:0xe26749066071b637!8m2!3d55.7073371!4d37.3859881!1m5!1m4!1s0x46b54d91de6fa877:0x96e21b5b19e3920a!8m2!3d55.658595899999995!4d37.4428281!1m5!1m4!1s0x414ab3cc65ff1f95:0x6261499f9a7afd91!8m2!3d55.6116874!4d37.686148599999996!3e0"
            ),
        )
    }

    @Test
    fun parse_directionsAddressWithSpace() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        name = "2088 Albion Rd @43.7481,-79.6332",
                        source = Source.URI,
                    )
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://maps.google.com/maps?f=d&daddr=2088 Albion Rd+@43.7481,-79.6332"
                )
            ),
            input.parse("https://maps.google.com/maps?f=d&daddr=2088 Albion Rd+@43.7481,-79.6332"),
        )
    }

    @Test
    fun parse_directionsEmpty() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/dir/"
                )
            ),
            input.parse("https://www.google.com/maps/dir/"),
        )
    }

    @Test
    fun parse_directionsEmptyPathPart() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        -31.9614112, 115.8523381,
                        z = 14.0,
                        name = "The Station, 1 Mends St, South Perth WA 6151",
                        source = Source.MAP_CENTER,
                    ),
                )
            ),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir//The+Station,+1+Mends+St,+South+Perth+WA+6151/@-31.9614112,115.8523381,14z/data=!4m6!4m5!1m0!1m2!1m1!1s0x2a32a529928d7447%3A0x4a1084749ffdee05!3e0!11m1!6b1?entry=ml&utm_campaign=ml-navnp-dr&coh=230964"),
        )
    }

    @Test
    fun parse_streetView() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(48.8584, 2.2945, source = Source.MAP_CENTER))),
            input.parse(@Suppress("SpellCheckingInspection") "https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120"),
        )
    }

    @Test
    fun parse_apiCenter() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        -33.712206, 150.311941,
                        z = 12.0,
                        source = Source.MAP_CENTER
                    )
                )
            ),
            input.parse("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206,150.311941&zoom=12&basemap=terrain"),
        )
    }

    @Test
    fun parse_apiCenterWithInvalidZoom() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        -33.712206, 150.311941,
                        source = Source.MAP_CENTER
                    )
                )
            ),
            input.parse("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206,150.311941&zoom=spam&basemap=terrain"),
        )
    }

    @Test
    fun parse_apiDirections() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(name = "Paris,France", source = Source.URI),
                    GCJ02MainlandChinaPoint(name = "Cherbourg,France", source = Source.URI),
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France"
                )
            ),
            input.parse("https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France"),
        )
    }

    @Test
    fun parse_apiViewpoint() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(48.857832, 2.295226, source = Source.MAP_CENTER))),
            input.parse("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832,2.295226&map_action=pano&api=1"),
        )
    }

    @Test
    fun parse_qParameterCoordinates() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(48.857832, 2.295226, source = Source.URI))),
            input.parse("https://www.google.com/maps?foo=bar&q=48.857832,2.295226&spam"),
        )
    }

    @Test
    fun parse_qParameterCoordinatesWithTrailingSlash() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(48.857832, 2.295226, source = Source.URI))),
            input.parse("https://www.google.com/maps/?q=48.857832,2.295226"),
        )
    }

    @Test
    fun parse_qParameterCoordinatesWithEmptyPath() = runTest {
        assertEquals(
            ParseResult(persistentListOf(GCJ02MainlandChinaPoint(39.797573, 18.370173, source = Source.URI))),
            input.parse("https://maps.google.com/?q=39.797573,18.370173&entry=gps&g_ep=abc&shorturl=1"),
        )
    }

    @Test
    fun parse_qParameterPlace() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "Central Park", source = Source.URI)),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/maps?foo=bar&q=Central Park&spam"
                )
            ),
            input.parse("https://www.google.com/maps?foo=bar&q=Central Park&spam"),
        )
    }

    @Test
    fun parse_qParameterPlaceWithoutPath() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        name = @Suppress("SpellCheckingInspection") "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach",
                        source = Source.URI,
                    )
                ),
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://maps.google.com?q=Caf%C3%A9+Heinemann,+Bismarckstra%C3%9Fe+91,+41061+M%C3%B6nchengladbach&ftid=0x47b8ac99b0a68bdd:0x8024629be3e9996&entry=gps&lucs=,94224825,94227247,94227248,47071704,47069508,94218641,94233073,94203019,47084304,94208458,94208447"
                )
            ),
            input.parse("https://maps.google.com?q=Caf%C3%A9+Heinemann,+Bismarckstra%C3%9Fe+91,+41061+M%C3%B6nchengladbach&ftid=0x47b8ac99b0a68bdd:0x8024629be3e9996&entry=gps&lucs=,94224825,94227247,94227248,47071704,47069508,94218641,94233073,94203019,47084304,94208458,94208447"),
        )
    }

    @Test
    fun parse_qParameterEmpty() = runTest {
        assertEquals(
            ParseResult(),
            input.parse("https://www.google.com/maps"),
        )
    }

    @Test
    fun parse_googleSearch() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://www.google.com/search?sca_esv=123&hl=en"
                )
            ),
            input.parse("https://www.google.com/search?sca_esv=123&hl=en"),
        )
    }

    @Test
    fun parse_myMaps() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsPlaceListInput,
                    "https://www.google.com/maps/d/edit?mid=XXX"
                )
            ),
            input.parse("https://www.google.com/maps/d/edit?mid=XXX"),
        )
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.googleMapsPlaceListInput,
                    "https://www.google.com/maps/d/viewer?mid=XXX"
                )
            ),
            input.parse("https://www.google.com/maps/d/viewer?mid=XXX"),
        )
    }

    @Test
    fun parse_http() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.5067296, 13.2599309,
                        z = 11.0,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER
                    )
                )
            ),
            input.parse("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }

    @Test
    fun parse_ukDomain() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.5067296, 13.2599309,
                        z = 11.0,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER
                    )
                )
            ),
            input.parse("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }

    @Test
    fun parse_noScheme() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    GCJ02MainlandChinaPoint(
                        52.5067296, 13.2599309,
                        z = 11.0,
                        name = "Berlin, Germany",
                        source = Source.MAP_CENTER
                    )
                )
            ),
            input.parse("maps.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"),
        )
    }
}
