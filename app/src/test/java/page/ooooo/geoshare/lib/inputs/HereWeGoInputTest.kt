package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class HereWeGoInputTest : BaseInputTest() {
    override val input = HereWeGoInput

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://wego.here.com/?map=50.21972,-0.68453,6.93"))
        assertTrue(doesUriPatternMatch("wego.here.com/?map=50.21972,-0.68453,6.93"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
        @Suppress("SpellCheckingInspection")
        assertTrue(doesUriPatternMatch("share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://wego.here.com/?map=50.21972,-0.68453,6.93"))
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() {
        assertNull(parseUri("https://wego.here.com"))
        assertNull(parseUri("https://wego.here.com/"))
        assertNull(parseUri("https://wego.here.com/?spam=1"))
    }

    @Test
    fun parseUri_coordinatesPath() {
        assertEquals(
            Position(Srs.WGS84, 52.536213398175434, 13.417085409306102),
            parseUri("https://share.here.com/l/52.536213398175434,13.417085409306102")
        )
    }

    @Test
    fun parseUri_coordinatesQueryParam() {
        assertEquals(
            Position(Srs.WGS84, 50.21972, -0.68453, z = 6.93),
            parseUri("https://wego.here.com/?map=50.21972,-0.68453,6.93")
        )
    }

    @Test
    fun parseUri_place() {
        assertEquals(
            Position(Srs.WGS84, 52.68444319987284, -8.623429663612297),
            parseUri("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=")
        )
    }

    @Test
    fun parseUri_placeAndCoordinates() {
        assertEquals(
            Position(Srs.WGS84, 52.68444319987284, -8.623429663612297, z = 16.0),
            parseUri("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=?map=52.68444,-8.62343,16")
        )
    }

    @Test
    fun parseUri_shortLink() {
        assertEquals(
            Position(Srs.WGS84, -38.14749, 145.14347),
            parseUri("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")
        )
    }
}
