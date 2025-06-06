package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before

import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.converters.GoogleMapsUrlConverter
import page.ooooo.geoshare.lib.converters.ParseHtmlResult
import page.ooooo.geoshare.lib.converters.ParseUrlResult
import java.net.URL

@Suppress("SpellCheckingInspection")
class GoogleMapsUrlConverterTest {

    private lateinit var googleMapsUrlConverter: GoogleMapsUrlConverter

    @Before
    fun before() {
        googleMapsUrlConverter = GoogleMapsUrlConverter(FakeLog(), FakeUriQuote())
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(googleMapsUrlConverter.isSupportedUrl(URL("ftp://www.google.com/maps/@52.5067296,13.2599309,6z")))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(googleMapsUrlConverter.isSupportedUrl(URL("https://www.example.com/")))
    }

    @Test
    fun isSupportedUrl_supportedFullUrl() {
        assertTrue(googleMapsUrlConverter.isSupportedUrl(URL("https://www.google.com/maps/@52.5067296,13.2599309,6z")))
    }

    @Test
    fun isSupportedUrl_supportedShortUrl() {
        assertTrue(googleMapsUrlConverter.isSupportedUrl(URL("https://maps.app.goo.gl/foo")))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/?spam=1")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_unknownPath() {
        assertNull(googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/spam")))
    }

    @Test
    fun parseUrl_coordinatesOnly() {
        assertEquals(
            "geo:52.5067296,13.2599309?z=6",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@52.5067296,13.2599309,6z")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_coordinatesOnlyStreetView() {
        assertEquals(
            "geo:53.512825,57.6891441",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@53.512825,57.6891441,0a,75y,90t/data=abc?utm_source=mstt_0&g_ep=def")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinates() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinatesWithManyDecimalPlaces() {
        assertEquals(
            "geo:44.448337599999995,26.0834555",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Strada+Occidentului+7,+Bucure%C8%99ti,+Romania/data=!4m6!3m5!1s0x40b201fdfa573623:0x4f53bb5ad3fdc97f!7e2!8m2!3d44.448337599999995!4d26.0834555?utm_source=mstt_1&entry=gps&coh=192189&g_ep=abc")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndNegativeCoordinates() {
        assertEquals(
            "geo:-17.2165721,-149.9470294?q=Berlin%2C%20Germany&z=11",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@-17.2165721,-149.9470294,11z/")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndIntegerCoordinates() {
        assertEquals(
            "geo:52,13?q=Berlin%2C%20Germany&z=11",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52,13,11z/")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndFractionalZoom() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=6",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,6.33z/")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndData() {
        assertEquals(
            "geo:40.785091,-73.968285?q=Central%20Park&z=15",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Central+Park/@40.785091,-73.968285,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString(),
        )
    }

    @Test
    fun parseUrl_placeAndPositiveCoordinatesAndPositiveDataCoordinates() {
        assertEquals(
            "geo:44.4490541,26.0888398?z=11",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate/@44.5190589,25.7489796,11.42z/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAndNegativeCoordinatesAndNegativeDataCoordinates() {
        assertEquals(
            "geo:40.785091,-73.968285?z=15",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Central+Park/@40.8,-73.9,15z/data=!3m1!4b1!4m5!3m4!1s0x89c2589a018531e3:0xb9df1f3170d990b5!8m2!3d40.785091!4d-73.968285")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString(),
        )
    }

    @Test
    fun parseUrl_placeAndPositiveDataCoordinates() {
        assertEquals(
            "geo:44.4490541,26.0888398",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/RAI+-+Romantic+%26+Intimate,+Calea+Victoriei+202+Bucure%C8%99ti,+Bucuresti+010098,+Rom%C3%A2nia/data=!4m6!3m5!1s0x40b1ffed911b9fcf:0x7394a7e7855d3929!8m2!3d44.4490541!4d26.0888398!16s%2Fg%2F11svmp0zhs")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAsCoordinates() {
        assertEquals(
            "geo:52.04,-2.35?z=15",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/maps/place/52.04,-2.35/@52.03877,-2.3416,15z/data=!3m1!1e3")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeAsCoordinatesWithPlus() {
        assertEquals(
            "geo:52.492611,13.431726?z=17",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/52.492611,+13.431726/@52.4929475,13.4317905,17z/data=!4m4!3m3!8m2?force=pwa")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeCoordinatesOnly() {
        assertEquals(
            "geo:52.03877,-2.3416",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/maps/place/52.03877,-2.3416/data=!3m1!1e3")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeOnly() {
        assertEquals(
            "geo:0,0?q=Pozna%C5%84%20Old%20Town%2C%2061-001%20Pozna%C5%84%2C%20Poland",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placeWithoutName() {
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_placelistsList() {
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/placelists/list/abcdef?g_ep=ghijkl%3D&g_st=isi")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_searchCoordinates() {
        assertEquals(
            "geo:48.8584,2.2945",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/48.8584,2.2945")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_searchPlace() {
        assertEquals(
            "geo:0,0?q=restaurants%20near%20me",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/restaurants+near+me")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_searchQueryCoordinates() {
        assertEquals(
            "geo:47.5951518,-122.3316393",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/?query_place_id=ChIJKxjxuaNqkFQR3CK6O1HNNqY&query=47.5951518%2C-122.3316393&api=1")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_searchQueryPlace() {
        assertEquals(
            "geo:0,0?q=centurylink%20field",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/search/?api=1&query=centurylink%2Bfield")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterQTakesPrecendence() {
        assertEquals(
            "geo:40.7128,-74.0060",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/?q=40.7128,-74.0060&viewpoint=34.0522,-118.2437")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:40.7128,-74.0060",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/?q=40.7128,-74.0060&center=34.0522,-118.2437")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_parameterDestinationTakesPrecendence() {
        assertEquals(
            "geo:0,0?q=Cherbourg%2CFrance",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/?destination=Cherbourg%2CFrance&q=Paris%2CFrance")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
        assertEquals(
            "geo:0,0?q=Cherbourg%2CFrance",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/?destination=Cherbourg%2CFrance&query=Paris%2CFrance")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_directionsCoordinates() {
        assertEquals(
            "geo:34.0522,-118.2437",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/40.7128,-74.0060/34.0522,-118.2437")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun toGeoUri_directionsFromTo() {
        assertEquals(
            "geo:0,0?q=Los%20Angeles%2C%20CA",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/New+York,+NY/Los+Angeles,+CA")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun toGeoUri_directionsFromToVia() {
        assertEquals(
            "geo:0,0?q=Washington%2C%20DC",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/New+York,+NY/Philadelphia,+PA/Washington,+DC")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun toGeoUri_directionsFromToWithData() {
        assertEquals(
            "geo:0,0?q=Potsdam",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/Berlin/Potsdam/data=abcd")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_directionsEmpty() {
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_streetView() {
        assertEquals(
            "geo:48.8584,2.2945",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@48.8584,2.2945,3a,75y,90t/data=!3m8!1e1!3m6!1sAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE!2e10!3e11!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipP5ELjVeDJfzgBQBp5XM-HsNU0Ep1k_KgE%3Dw203-h100-k-no-pi-0-ya293.79999-ro-0-fo100!7i10240!8i5120")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_apiCenter() {
        assertEquals(
            "geo:-33.712206,150.311941?z=12",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206%2C150.311941&zoom=12&basemap=terrain")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_apiCenterWithInvalidZoom() {
        assertEquals(
            "geo:-33.712206,150.311941",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@?api=1&map_action=map&center=-33.712206%2C150.311941&zoom=spam&basemap=terrain")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_apiDirections() {
        assertEquals(
            "geo:0,0?q=Cherbourg%2CFrance",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/dir/?api=1&origin=Paris%2CFrance&destination=Cherbourg%2CFrance&travelmode=driving&waypoints=Versailles%2CFrance%7CChartres%2CFrance%7CLe%2BMans%2CFrance%7CCaen%2CFrance")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_apiViewpoint() {
        assertEquals(
            "geo:48.857832,2.295226",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/@?fov=80&pitch=38&heading=-45&viewpoint=48.857832%2C2.295226&map_action=pano&api=1")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_qParameterCoordinates() {
        assertEquals(
            "geo:48.857832,2.295226",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps?foo=bar&q=48.857832%2C2.295226&spam")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_qParameterCoordinatesWithTrailingSlash() {
        assertEquals(
            "geo:48.857832,2.295226",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps/?q=48.857832%2C2.295226")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_qParameterCoordinatesWithEmptyPath() {
        assertEquals(
            "geo:39.797573,18.370173",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com/?q=39.797573,18.370173&entry=gps&g_ep=abc&shorturl=1")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_qParameterPlace() {
        assertEquals(
            "geo:0,0?q=Central%20Park",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps?foo=bar&q=Central%20Park&spam")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_qParameterPlaceWithoutPath() {
        assertEquals(
            "geo:0,0?q=Caf%C3%A9%20Heinemann%2C%20Bismarckstra%C3%9Fe%2091%2C%2041061%20M%C3%B6nchengladbach",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.com?q=Caf%C3%A9+Heinemann,+Bismarckstra%C3%9Fe+91,+41061+M%C3%B6nchengladbach")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_qParameterEmpty() {
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/maps")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_googleSearch() {
        assertEquals(
            "geo:0,0",
            (googleMapsUrlConverter.parseUrl(URL("https://www.google.com/search?sca_esv=123&hl=en")) as ParseUrlResult.RequiresHtmlParsingToGetCoords).geoUriBuilder
                .toString()
        )
    }

    @Test
    fun parseUrl_http() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            (googleMapsUrlConverter.parseUrl(URL("http://www.google.com/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString(),
        )
    }

    @Test
    fun parseUrl_ukDomain() {
        assertEquals(
            "geo:52.5067296,13.2599309?q=Berlin%2C%20Germany&z=11",
            (googleMapsUrlConverter.parseUrl(URL("https://maps.google.co.uk/maps/place/Berlin,+Germany/@52.5067296,13.2599309,11z/data=12345?entry=ttu&g_ep=678910")) as ParseUrlResult.Parsed).geoUriBuilder
                .toString(),
        )
    }

    @Test
    fun parseHtml_link() {
        val html =
            this.javaClass.classLoader!!.getResource("TmbeHMiLEfTBws9EA.html")!!
                .readText()
        assertEquals(
            "geo:44.4490541,26.0888398",
            (googleMapsUrlConverter.parseHtml(html) as ParseHtmlResult.Parsed).geoUriBuilder.toString()
        )
    }

    @Test
    fun parseHtml_array() {
        val html =
            this.javaClass.classLoader!!.getResource("mfmnkPs6RuGyp0HOmXLSKg.html")!!
                .readText()
        assertEquals(
            "geo:59.1293656,11.4585672",
            (googleMapsUrlConverter.parseHtml(html) as ParseHtmlResult.Parsed).geoUriBuilder.toString()
        )
    }

    @Test
    fun parseHtml_failure() {
        assertNull(googleMapsUrlConverter.parseHtml("spam"))
    }

    @Test
    fun parseHtml_googleSearchHtmlDoesNotContainUrl_returnsNull() {
        assertNull(googleMapsUrlConverter.parseHtml("<html></html>"))
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsRelativeUrl_returnsAbsoluteUrl() {
        val html = this.javaClass.classLoader!!.getResource("91UYXud.html")!!
            .readText()
        assertEquals(
            URL("https://www.google.com/maps/place//data=!4m2!3m1!1s0xc3f7d4e21a00705:0xa9ea51361ed84bda?sa=X&amp;ved=2ahUKEwiY7vv80aeKAxU41QIHHSgBOlsQ4kB6BAgHEAA&amp;hl=de&amp;gl=de"),
            (googleMapsUrlConverter.parseHtml(html) as ParseHtmlResult.Redirect).url
        )
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsAbsoluteUrl_returnsIt() {
        val html = """<html><a href="" data-url="https://www.example.com/foo"></a></html>"""
        assertEquals(
            URL("https://www.example.com/foo"),
            (googleMapsUrlConverter.parseHtml(html) as ParseHtmlResult.Redirect).url
        )
    }

    @Test
    fun parseHtml_googleSearchHtmlContainsInvalidUrl_returnsNull() {
        val html = """<html><a href="" data-url="abc"></a></html>"""
        assertNull(googleMapsUrlConverter.parseHtml(html))
    }

    @Test
    fun isShortUri_mapsAppGooGlCorrect() {
        assertTrue(googleMapsUrlConverter.isShortUrl(URL("https://maps.app.goo.gl/foo")))
    }

    @Test
    fun isShortUri_mapsAppGooGlWithQueryStringCorrect() {
        assertTrue(googleMapsUrlConverter.isShortUrl(URL("https://maps.app.goo.gl/foo?g_st=isi")))
    }

    @Test
    fun isShortUri_mapsAppGooGlMissingPath() {
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://maps.app.goo.gl/")))
    }

    @Test
    fun isShortUri_appGooGlCorrect() {
        assertTrue(googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps/foo")))
    }

    @Test
    fun isShortUri_appGooGlWrongPath() {
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps")))
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/maps/")))
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://app.goo.gl/foo/bar")))
    }

    @Test
    fun isShortUri_gooGlCorrect() {
        assertTrue(googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps/foo")))
    }

    @Test
    fun isShortUri_gooGlWrongPath() {
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps")))
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/maps/")))
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://goo.gl/foo/bar")))
    }

    @Test
    fun isShortUri_gCoCorrect() {
        assertTrue(googleMapsUrlConverter.isShortUrl(URL("https://g.co/kgs/foo")))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(googleMapsUrlConverter.isShortUrl(URL("https://www.example.com/foo")))
    }
}
