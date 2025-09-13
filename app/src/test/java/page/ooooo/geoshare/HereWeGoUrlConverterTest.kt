package page.ooooo.geoshare

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.converters.HereWeGoUrlConverter
import java.net.URL

class HereWeGoUrlConverterTest : BaseUrlConverterTest() {
    @Before
    fun before2() {
        urlConverter = HereWeGoUrlConverter()
    }

    @Test
    fun isSupportedUrl_unknownProtocol() {
        assertFalse(isSupportedUrl("ftp://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
    }

    @Test
    fun isSupportedUrl_unknownHost() {
        assertFalse(isSupportedUrl("https://www.example.com/"))
    }

    @Test
    fun isSupportedUrl_supportedUrl() {
        assertTrue(isSupportedUrl("https://wego.here.com/?map=50.21972,-0.68453,6.93"))
    }

    @Test
    fun isSupportedUrl_shortUrl() {
        assertTrue(isSupportedUrl("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://wego.here.com"))
        assertNull(parseUrl("https://wego.here.com/"))
        assertNull(parseUrl("https://wego.here.com/?spam=1"))
    }

    @Test
    fun parseUrl_coordinates() {
        assertEquals(
            Position("50.21972", "-0.68453", z = "7"),
            parseUrl("https://wego.here.com/?map=50.21972,-0.68453,6.93")
        )
    }

    @Test
    fun parseUrl_place() {
        assertEquals(
            Position("52.68444319987284", "-8.623429663612297"),
            parseUrl("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=")
        )
    }

    @Test
    fun parseUrl_placeAndCoordinates() {
        assertEquals(
            Position("52.68444319987284", "-8.623429663612297", z = "16"),
            parseUrl("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=?map=52.68444,-8.62343,16")
        )
    }

    @Test
    fun parseUrl_shortLink() {
        assertEquals(
            Position("-38.14749", "145.14347"),
            parseUrl("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")
        )
    }

    @Test
    fun parseHtml_alwaysReturnsNull() {
        assertNull(parseHtml("<html></html>"))
    }

    @Test
    fun isShortUrl_alwaysReturnsFalse() {
        assertFalse(isShortUrl("https://maps.apple/p/7E-Brjrk_THN14"))
    }
}
