package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class HereWeGoInputTest : InputTest {
    override val input = HereWeGoInput

    @Test
    fun uriPattern_fullUrl() {
        assertEquals(
            "https://wego.here.com/?map=50.21972,-0.68453,6.93",
            getUri("https://wego.here.com/?map=50.21972,-0.68453,6.93")
        )
        assertEquals("wego.here.com/?map=50.21972,-0.68453,6.93", getUri("wego.here.com/?map=50.21972,-0.68453,6.93"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertEquals(
            "https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=",
            getUri("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")
        )
        @Suppress("SpellCheckingInspection") assertEquals(
            "share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=",
            getUri("share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")
        )
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "wego.here.com/?map=50.21972,-0.68453,6.93",
            getUri("ftp://wego.here.com/?map=50.21972,-0.68453,6.93"),
        )
    }

    @Test
    fun uriPattern_spaces() {
        assertEquals(
            "https://wego.here.com/?q=foobar",
            getUri("https://wego.here.com/?q=foobar ")
        )
        assertEquals(
            "https://wego.here.com/?q=foo bar",
            getUri("https://wego.here.com/?q=foo bar ")
        )
        assertEquals(
            "https://wego.here.com/?q=foo",
            getUri("https://wego.here.com/?q=foo  bar")
        )
        assertEquals(
            "https://wego.here.com/?q=foo",
            getUri("https://wego.here.com/?q=foo\tbar")
        )
    }

    @Test
    fun parseUri_unknownPathOrParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://wego.here.com"))
        assertEquals(ParseUriResult(), parseUri("https://wego.here.com/"))
        assertEquals(ParseUriResult(), parseUri("https://wego.here.com/?spam=1"))
    }

    @Test
    fun parseUri_coordinatesPath() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.536213398175434, 13.417085409306102, source = Source.URI))),
            parseUri("https://share.here.com/l/52.536213398175434,13.417085409306102"),
        )
    }

    @Test
    fun parseUri_coordinatesQueryParam() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(50.21972, -0.68453, z = 6.93, source = Source.MAP_CENTER))),
            parseUri("https://wego.here.com/?map=50.21972,-0.68453,6.93"),
        )
    }

    @Test
    fun parseUri_place() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(52.68444319987284, -8.623429663612297, source = Source.HASH))),
            parseUri("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s="),
        )
    }

    @Test
    fun parseUri_placeAndCoordinates() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    WGS84Point(
                        52.68444319987284, -8.623429663612297,
                        z = 16.0,
                        source = Source.HASH,
                    )
                )
            ),
            parseUri("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=?map=52.68444,-8.62343,16"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult(persistentListOf(WGS84Point(-38.14749, 145.14347, source = Source.HASH))),
            parseUri("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="),
        )
    }
}
