package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import java.net.URL

@Suppress("SpellCheckingInspection")
class GoogleMapsUrlConverterTest : BaseUrlConverterTest() {

    @Before
    fun before2() {
        urlConverter = GoogleMapsUrlConverter()
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertTrue(isSupportedUrl(URL("ftp://www.google.com/maps/@52.5067296,13.2599309,6z")))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl(URL("https://www.example.com/")))
    }

    @Test
    fun isSupportedUrl_supportedFullUrl() {
        assertTrue(isSupportedUrl(URL("https://www.google.com/maps/@52.5067296,13.2599309,6z")))
    }

    @Test
    fun isSupportedUrl_supportedShortUrl() {
        assertTrue(isSupportedUrl(URL("https://maps.app.goo.gl/foo")))
        assertTrue(isSupportedUrl(URL("https://app.goo.gl/maps/foo")))
        assertTrue(isSupportedUrl(URL("https://g.co/kgs/foo")))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://maps.google.com"))
        )
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://maps.google.com/"))
        )
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://maps.google.com/?spam=1"))
        )
    }

    @Test
    fun parseUrl_unknownPath() {
        assertNull(parseUrl(URL("https://maps.google.com/spam")))
    }

    @Test
    fun parseUrl_coordinatesOnly() {
        assertEquals(
            Position("52.5067296", "13.2599309", z = "6"),
            parseUrl(URL("https://www.google.com/maps/@52.5067296,13.2599309,6z"))
        )
    }

    @Test
    fun parseUrl_coordinatesOnlyStreetView() {
        assertEquals(
            Position("53.512825", "57.6891441"),
            parseUrl(URL("https://www.google.com/maps/@53.512825,57.6891441,0a,75y,90t/data=abc?utm_source=mstt_0&g_ep=def"))
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinates() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910"))
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinatesWithManyDecimalPlaces() {
        assertEquals(
            Position("44.448337599999995", "26.0834555"),
            parseUrl(URL("https://www.google.com/maps/place/Strada+Occidentului+7,+Bucure%C8%99ti,+Romania/data=!4m6!3m5!1s0x40b201fdfa573623:0x4f53bb5ad3fdc97f!7e2!8m2!3d44.448337599999995!4d26.0834555?utm_source=mstt_1&entry=gps&coh=192189&g_ep=abc"))
        )
    }

    @Test
    fun parseUrl_placeAndNegativeCoordinates() {
        assertEquals(
            Position("-17.2165721", "-149.9470294", q = "Berlin, Germany", z = "11"),
            parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/"))
        )
    }

    @Test
    fun parseUrl_placeAndIntegerCoordinates() {
        assertEquals(
            Position("52", "13", q = "Berlin, Germany", z = "11"),
            parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/"))
        )
    }

    @Test
    fun parseUrl_placeAndFractionalZoom() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "6"),
            parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/"))
        )
    }

    @Test
    fun parseUrl_placeAndData() {
        assertEquals(
            Position("40.785091", "-73.968285", q = "Central Park", z = "15"),
            parseUrl(URL("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2")),
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinatesAndPositiveDataCoordinates() {
        assertEquals(
            Position("44.4490541", "26.0888398", z = "11"),
            parseUrl(URL("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate/@44.5190589,25.7489796,11.42z/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs"))

        )
    }

    @Test
    fun parseUrl_placeAndNegativeCoordinatesAndNegativeDataCoordinates() {
        assertEquals(
            Position("40.785091", "-73.968285", z = "15"),
            parseUrl(URL("https://www.google.com/maps/place/Central+Park/@40.8,-73.9,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285")),
        )
    }

    @Test
    fun parseUrl_placeAndPositiveDataCoordinates() {
        assertEquals(
            Position("44.4490541", "26.0888398"),
            parseUrl(URL("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate,+Calea+Victoriei+202+Bucure%C8%99ti,+Bucuresti+010098,+Rom%C3%A2nia/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs"))

        )
    }

    @Test
    fun parseUrl_placeAsCoordinates() {
        assertEquals(
            Position("52.04", "-2.35", z = "15"),
            parseUrl(URL("https://maps.google.com/maps/place/52.04,-2.35/@52.03877,-2.3416,15z/data=!3m1!1e3"))

        )
    }

    @Test
    fun parseUrl_placeAsCoordinatesWithPlus() {
        assertEquals(
            Position("52.492611", "13.431726", z = "17"),
            parseUrl(URL("https://www.google.com/maps/place/52.492611,+13.431726/@52.4929475,13.4317905,17z/data=!4m4!3m3!8m2?force=pwa"))
        )
    }

    @Test
    fun parseUrl_placeCoordinatesOnly() {
        assertEquals(
            Position("52.03877", "-2.3416"),
            parseUrl(URL("https://maps.google.com/maps/place/52.03877,-2.3416/data=!3m1!1e3"))
        )
    }

    @Test
    fun parseUrl_placeOnly() {
        assertEquals(
            Position(null, null, q = "Poznań Old Town, 61-001 Poznań, Poland"),
            parseUrl(URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd"))
        )
    }

    @Test
    fun parseUrl_placeWithoutName() {
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda"))
        )
    }

    @Test
    fun parseUrl_placelistsList() {
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://www.google.com/maps/placelists/list/abcdef?g_ep=ghijkl%3D&g_st=isi"))
        )
    }

    @Test
    fun parseUrl_searchCoordinates() {
        assertEquals(
            Position("48.8584", "2.2945"),
            parseUrl(URL("https://www.google.com/maps/search/48.8584,2.2945"))
        )
    }

    @Test
    fun parseUrl_searchPlace() {
        assertEquals(
            Position(null, null, q = "restaurants near me"),
            parseUrl(URL("https://www.google.com/maps/search/restaurants+near+me"))
        )
    }

    @Test
    fun parseUrl_searchQueryCoordinates() {
        assertEquals(
            Position("47.5951518", "-122.3316393"),
            parseUrl(URL("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518,-122.3316393&api=1"))
        )
    }

    @Test
    fun parseUrl_searchQueryPlace() {
        assertEquals(
            Position(null, null, q = "centurylink+field"),
            parseUrl(URL("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield"))
        )
    }

    @Test
    fun parseUrl_parameterQTakesPrecendence() {
        assertEquals(
            Position("40.7128", "-74.0060"),
            parseUrl(URL("https://www.google.com/?q=40.7128,-74.0060&viewpoint=34.0522,-118.2437"))
        )
        assertEquals(
            Position("40.7128", "-74.0060"),
            parseUrl(URL("https://www.google.com/?q=40.7128,-74.0060&center=34.0522,-118.2437"))
        )
    }

    @Test
    fun parseUrl_parameterDestinationTakesPrecendence() {
        assertEquals(
            Position(null, null, q = "Cherbourg,France"),
            parseUrl(URL("https://www.google.com/?destination=Cherbourg,France&q=Paris,France"))
        )
        assertEquals(
            Position(null, null, q = "Cherbourg,France"),
            parseUrl(URL("https://www.google.com/?destination=Cherbourg,France&query=Paris,France"))
        )
    }

    @Test
    fun parseUrl_directionsCoordinates() {
        assertEquals(
            Position("34.0522", "-118.2437"),
            parseUrl(URL("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437"))
        )
    }

    @Test
    fun parseUrl_directionsPlaceAndCoordinates() {
        assertEquals(
            Position("48.83887481689453", "2.2740750312805176", z = "8"),
            parseUrl(URL("https://www.google.com/maps/dir/My+location/H%c3%b4pital+Europ%c3%a9en+Georges+Pompidou+Assistance+Publique-H%c3%b4pitaux+de+Paris,20+r+Leblanc%2c+75015+Paris/@48.83887481689453,2.2740750312805176,8z/"))
        )
    }

    @Test
    fun toGeoUri_directionsFromTo() {
        assertEquals(
            Position(null, null, q = "Los Angeles, CA"),
            parseUrl(URL("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA"))
        )
    }

    @Test
    fun toGeoUri_directionsFromToVia() {
        assertEquals(
            Position(null, null, q = "Washington, DC"),
            parseUrl(URL("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC"))
        )
    }

    @Test
    fun toGeoUri_directionsFromToWithData() {
        assertEquals(
            Position(null, null, q = "Potsdam"),
            parseUrl(URL("https://www.google.com/maps/dir/Berlin/Potsdam/data=abcd"))
        )
    }

    @Test
    fun parseUrl_directionsEmpty() {
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://www.google.com/maps/dir/"))
        )
    }

    @Test
    fun parseUrl_streetView() {
        assertEquals(
            Position("48.8584", "2.2945"),
            parseUrl(URL("https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120"))
        )
    }

    @Test
    fun parseUrl_apiCenter() {
        assertEquals(
            Position("-33.712206", "150.311941", z = "12"),
            parseUrl(URL("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206,150.311941&zoom=12&basemap=terrain"))
        )
    }

    @Test
    fun parseUrl_apiCenterWithInvalidZoom() {
        assertEquals(
            Position("-33.712206", "150.311941"),
            parseUrl(URL("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206,150.311941&zoom=spam&basemap=terrain"))
        )
    }

    @Test
    fun parseUrl_apiDirections() {
        assertEquals(
            Position(null, null, q = "Cherbourg,France"),
            parseUrl(URL("https://www.google.com/maps/dir/?api=1&origin=Paris,France&destination=Cherbourg,France&travelmode=driving&waypoints=Versailles,France%7CChartres,France%7CLe%2BMans,France%7CCaen,France"))
        )
    }

    @Test
    fun parseUrl_apiViewpoint() {
        assertEquals(
            Position("48.857832", "2.295226"),
            parseUrl(URL("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832,2.295226&map_action=pano&api=1"))
        )
    }

    @Test
    fun parseUrl_qParameterCoordinates() {
        assertEquals(
            Position("48.857832", "2.295226"),
            parseUrl(URL("https://www.google.com/maps?foo=bar&q=48.857832,2.295226&spam"))
        )
    }

    @Test
    fun parseUrl_qParameterCoordinatesWithTrailingSlash() {
        assertEquals(
            Position("48.857832", "2.295226"),
            parseUrl(URL("https://www.google.com/maps/?q=48.857832,2.295226"))
        )
    }

    @Test
    fun parseUrl_qParameterCoordinatesWithEmptyPath() {
        assertEquals(
            Position("39.797573", "18.370173"),
            parseUrl(URL("https://maps.google.com/?q=39.797573,18.370173&entry=gps&g_ep=abc&shorturl=1"))
        )
    }

    @Test
    fun parseUrl_qParameterPlace() {
        assertEquals(
            Position(null, null, q = "Central Park"),
            parseUrl(URL("https://www.google.com/maps?foo=bar&q=Central Park&spam"))
        )
    }

    @Test
    fun parseUrl_qParameterPlaceWithoutPath() {
        assertEquals(
            Position(null, null, q = "Café Heinemann, Bismarckstraße 91, 41061 Mönchengladbach"),
            parseUrl(URL("https://maps.google.com?q=Caf%C3%A9+Heinemann,+Bismarckstra%C3%9Fe+91,+41061+M%C3%B6nchengladbach"))
        )
    }

    @Test
    fun parseUrl_qParameterEmpty() {
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://www.google.com/maps"))
        )
    }

    @Test
    fun parseUrl_googleSearch() {
        assertEquals(
            Position(null, null),
            parseUrl(URL("https://www.google.com/search?sca_esv=123&hl=en"))
        )
    }

    @Test
    fun parseUrl_http() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl(URL("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")),
        )
    }

    @Test
    fun parseUrl_ukDomain() {
        assertEquals(
            Position("52.5067296", "13.2599309", q = "Berlin, Germany", z = "11"),
            parseUrl(URL("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")),
        )
    }

    @Test
    fun parseHtml_link() {
        val html = this.javaClass.classLoader!!.getResource("TmbeHMiLEfTBws9EA.html")!!.readText()
        assertEquals(Position("44.4490541", "26.0888398"), parseHtml(html))
    }

    @Test
    fun parseHtml_array() {
        val html = this.javaClass.classLoader!!.getResource("mfmnkPs6RuGyp0HOmXLSKg.html")!!.readText()
        assertEquals(Position("59.1293656", "11.4585672"), parseHtml(html))
    }

    @Test
    fun parseHtml_failure() {
        assertNull(parseHtml("spam"))
    }

    @Test
    fun parseHtml_googleSearchHtmlDoesNotContainUrl_returnsNull() {
        assertNull(parseHtml("<html></html>"))
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsRelativeUrl_returnsAbsoluteUrl() {
        val html = this.javaClass.classLoader!!.getResource("91UYXud.html")!!.readText()
        assertEquals(
            "https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=2ahUKEwiY7vv80aeKAxU41QIHHSgBOlsQ4kB6BAgHEAA&amp;hl=de&amp;gl=de",
            parseHtmlRedirect(html)
        )
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsAbsoluteUrl_returnsIt() {
        val html = """<html><a href="" data-url="https://www.example.com/foo"></a></html>"""
        assertEquals(
            "https://www.example.com/foo",
            parseHtmlRedirect(html)
        )
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsInvalidUrl_returnsNull() {
        val html = """<html><a href="" data-url="abc"></a></html>"""
        assertNull(parseHtml(html))
    }

    @Test
    fun isShortUri_mapsAppGooGlCorrect() {
        assertTrue(isShortUrl(URL("https://maps.app.goo.gl/foo")))
    }

    @Test
    fun isShortUri_mapsAppGooGlWithQueryStringCorrect() {
        assertTrue(isShortUrl(URL("https://maps.app.goo.gl/foo?g_st=isi")))
    }

    @Test
    fun isShortUri_mapsAppGooGlMissingPath() {
        assertFalse(isShortUrl(URL("https://maps.app.goo.gl/")))
    }

    @Test
    fun isShortUri_appGooGlCorrect() {
        assertTrue(isShortUrl(URL("https://app.goo.gl/maps/foo")))
    }

    @Test
    fun isShortUri_appGooGlWrongPath() {
        assertTrue(isShortUrl(URL("https://app.goo.gl/maps")))
        assertTrue(isShortUrl(URL("https://app.goo.gl/maps/")))
        assertTrue(isShortUrl(URL("https://app.goo.gl/foo/bar")))
    }

    @Test
    fun isShortUri_gooGlCorrect() {
        assertTrue(isShortUrl(URL("https://goo.gl/maps/foo")))
    }

    @Test
    fun isShortUri_gooGlWrongPath() {
        assertTrue(isShortUrl(URL("https://goo.gl/maps")))
        assertTrue(isShortUrl(URL("https://goo.gl/maps/")))
        assertTrue(isShortUrl(URL("https://goo.gl/foo/bar")))
    }

    @Test
    fun isShortUri_gCoCorrect() {
        assertTrue(isShortUrl(URL("https://g.co/kgs/foo")))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUrl(URL("https://www.example.com/foo")))
    }
}
