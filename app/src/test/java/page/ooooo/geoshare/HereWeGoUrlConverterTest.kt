package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.HereWeGoUrlConverter
import page.ooooo.geoshare.lib.converters.ParseUrlResult
import java.net.URL

class HereWeGoUrlConverterTest {

    private lateinit var hereWeGoUrlConverter: HereWeGoUrlConverter

    @Before
    fun before() {
        hereWeGoUrlConverter = HereWeGoUrlConverter(FakeLog(), FakeUriQuote())
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(hereWeGoUrlConverter.isSupportedUrl(URL("ftp://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(hereWeGoUrlConverter.isSupportedUrl(URL("https://www.example.com/")))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(hereWeGoUrlConverter.isSupportedUrl(URL("https://wego.here.com/?map=50.21972,-0.68453,6.93")))
    }

    @Test
    fun isSupportedUrl_shortUrl() {
        assertTrue(hereWeGoUrlConverter.isSupportedUrl(URL("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(hereWeGoUrlConverter.parseUrl(URL("https://wego.here.com")))
        assertNull(hereWeGoUrlConverter.parseUrl(URL("https://wego.here.com/")))
        assertNull(hereWeGoUrlConverter.parseUrl(URL("https://wego.here.com/?spam=1")))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            ParseUrlResult.Parsed(Position("50.21972", "-0.68453", z = "7")),
            hereWeGoUrlConverter.parseUrl(URL("https://wego.here.com/?map=50.21972,-0.68453,6.93"))
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            ParseUrlResult.Parsed(Position("52.68444319987284", "-8.623429663612297")),
            hereWeGoUrlConverter.parseUrl(URL("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s="))
        )
    }

    @Test
    fun parseUrl_placeAndCoordinates() {
        assertEquals(
            ParseUrlResult.Parsed(Position("52.68444319987284", "-8.623429663612297", z = "16")),
            hereWeGoUrlConverter.parseUrl(URL("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=?map=52.68444,-8.62343,16"))
        )
    }

    @Test
    fun parseUrl_shortLink() {
        assertEquals(
            ParseUrlResult.Parsed(Position("-38.14749", "145.14347")),
            hereWeGoUrlConverter.parseUrl(URL("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
        )
    }

    @Test
    fun parseHtml_alwaysReturnsNull() {
        assertNull(hereWeGoUrlConverter.parseHtml("<html></html>"))
    }

    @Test
    fun isShortUrl_alwaysReturnsFalse() {
        assertFalse(hereWeGoUrlConverter.isShortUrl(URL("https://maps.apple/p/7E-Brjrk_THN14")))
    }
}
