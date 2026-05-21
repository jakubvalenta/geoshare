package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class HereWeGoUriInputTest : InputTest {
    private val input = FakeInputRepository.hereWeGoUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://wego.here.com/?map=50.21972,-0.68453,6.93",
            input.match("https://wego.here.com/?map=50.21972,-0.68453,6.93")
        )
        assertEquals(
            "wego.here.com/?map=50.21972,-0.68453,6.93",
            input.match("wego.here.com/?map=50.21972,-0.68453,6.93")
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals(
            "https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=",
            input.match("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")
        )
        @Suppress("SpellCheckingInspection") assertEquals(
            "share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=",
            input.match("share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30=")
        )
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "wego.here.com/?map=50.21972,-0.68453,6.93",
            input.match("ftp://wego.here.com/?map=50.21972,-0.68453,6.93"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://wego.here.com/?q=foobar",
            input.match("https://wego.here.com/?q=foobar ")
        )
        assertEquals(
            "https://wego.here.com/?q=foo bar",
            input.match("https://wego.here.com/?q=foo bar ")
        )
        assertEquals(
            "https://wego.here.com/?q=foo",
            input.match("https://wego.here.com/?q=foo  bar")
        )
        assertEquals(
            "https://wego.here.com/?q=foo",
            input.match("https://wego.here.com/?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult(), input.parse("https://wego.here.com"))
        assertEquals(ParseResult(), input.parse("https://wego.here.com/"))
        assertEquals(ParseResult(), input.parse("https://wego.here.com/?spam=1"))
    }

    @Test
    fun parse_coordinatesPath() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.536213398175434, 13.417085409306102, source = Source.URI))),
            input.parse("https://share.here.com/l/52.536213398175434,13.417085409306102"),
        )
    }

    @Test
    fun parse_coordinatesQueryParam() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(50.21972, -0.68453, z = 6.93, source = Source.MAP_CENTER))),
            input.parse("https://wego.here.com/?map=50.21972,-0.68453,6.93"),
        )
    }

    @Test
    fun parse_place() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(52.68444319987284, -8.623429663612297, source = Source.HASH))),
            input.parse("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s="),
        )
    }

    @Test
    fun parse_placeAndCoordinates() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        52.68444319987284, -8.623429663612297,
                        z = 16.0,
                        source = Source.HASH,
                    )
                )
            ),
            input.parse("https://wego.here.com/p/s-aWQ9O2xhdD01Mi42ODQ0NDMxOTk4NzI4NDtsb249LTguNjIzNDI5NjYzNjEyMjk3O249TGltZXJpY2s=?map=52.68444,-8.62343,16"),
        )
    }

    @Test
    fun parse_shortLink() = runTest {
        assertEquals(
            ParseResult(persistentListOf(WGS84Point(-38.14749, 145.14347, source = Source.HASH))),
            input.parse("https://share.here.com/p/e-eyJ2ZXJzaW9uIjoiMS4wLjMiLCJwcm92aWRlcklkIjoiMDM2OGx4eDUtYWNkYjgxOGNlNjU1MDc2OTY2ZTU0NThhZTRkZWRkM2MiLCJsYXRpdHVkZSI6LTM4LjE0NzQ5LCJsb25naXR1ZGUiOjE0NS4xNDM0N30="),
        )
    }
}
