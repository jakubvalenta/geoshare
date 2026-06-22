package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository

class GoogleMapsShortLinkInputTest : InputTest {
    private val input = FakeInputRepository.googleMapsShortLinkInput

    @Test
    fun match_shortLink() {
        assertEquals("https://maps.app.goo.gl/foo", input.match("https://maps.app.goo.gl/foo"))
        assertEquals("https://app.goo.gl/maps/foo", input.match("https://app.goo.gl/maps/foo"))
        assertEquals("https://g.co/kgs/foo", input.match("https://g.co/kgs/foo"))
        assertEquals("maps.app.goo.gl/foo", input.match("maps.app.goo.gl/foo"))
        assertEquals("app.goo.gl/maps/foo", input.match("app.goo.gl/maps/foo"))
        assertEquals("g.co/kgs/foo", input.match("g.co/kgs/foo"))
    }

    @Test
    fun match_shortLinkWithQueryString() {
        assertEquals(
            "https://maps.app.goo.gl/foo",
            input.match("https://maps.app.goo.gl/foo?g_st=isi"),
        )
    }

    @Test
    fun match_shortLinkWithInvalidTrailingCharacters() {
        assertEquals(
            "https://maps.app.goo.gl/jVuuNEZ_-FQ3UGhX7",
            input.match("https://maps.app.goo.gl/jVuuNEZ_-FQ3UGhX7%3C/a%3E%3C/p%3E"),
        )
    }

    @Test
    fun match_shortLinkWithLinkParam() {
        assertNull(
            input.match("https://maps.app.goo.gl/?utm_campaign=ml-navnp-dr&pcampaignid=ml-navnp-dr&apn=com.google.android.apps.maps&amv=965100030&link=https%3A%2F%2Fwww.google.com%2Fmaps%2Fdir%2F%2FThe%2BStation%2C%2B1%2BMends%2BSt%2C%2BSouth%2BPerth%2BWA%2B6151%2F%40-31.9614112%2C115.8523381%2C14z%2Fdata%3D!4m6!4m5!1m0!1m2!1m1!1s0x2a32a529928d7447%3A0x4a1084749ffdee05!3e0!11m1!6b1%3Fentry%3Dml%26utm_campaign%3Dml-navnp-dr%26coh%3D230964&isi=585027354&ibi=com.google.Maps&ius=comgooglemapsurl&pt=9008&mt=8&ct=ml-navnp-dr&ifl=https%3A%2F%2Fapps.apple.com%2Fapp%2Fid585027354%3Fct%3Dml-navnp-dr%26pt%3D9008%26mt%3D8%26ppid%3Da2beb448-b2c7-4ed7-ae8b-78fdcf9f33a8&efr=1")
        )
    }

    @Test
    fun match_mapsAppGooGlCorrect() {
        assertEquals("https://maps.app.goo.gl/foo", input.match("https://maps.app.goo.gl/foo"))
    }

    @Test
    fun match_mapsAppGooGlWithQueryStringCorrect() {
        assertEquals("https://maps.app.goo.gl/foo", input.match("https://maps.app.goo.gl/foo?g_st=isi"))
    }

    @Test
    fun match_mapsAppGooGlMissingPath() {
        assertNull(input.match("https://maps.app.goo.gl/"))
    }

    @Test
    fun match_appGooGlCorrect() {
        assertEquals("https://app.goo.gl/maps/foo", input.match("https://app.goo.gl/maps/foo"))
    }

    @Test
    fun match_appGooGlUnknownPath() {
        assertEquals("https://app.goo.gl/maps", input.match("https://app.goo.gl/maps"))
        assertEquals("https://app.goo.gl/maps/", input.match("https://app.goo.gl/maps/"))
        assertEquals("https://app.goo.gl/foo/bar", input.match("https://app.goo.gl/foo/bar"))
    }

    @Test
    fun match_gooGlCorrect() {
        assertEquals("https://goo.gl/maps/foo", input.match("https://goo.gl/maps/foo"))
    }

    @Test
    fun match_gooGlUnknownPath() {
        assertEquals("https://goo.gl/maps", input.match("https://goo.gl/maps"))
        assertEquals("https://goo.gl/maps/", input.match("https://goo.gl/maps/"))
        assertEquals("https://goo.gl/foo/bar", input.match("https://goo.gl/foo/bar"))
    }

    @Test
    fun match_gCoCorrect() {
        assertEquals("https://g.co/kgs/foo", input.match("https://g.co/kgs/foo"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/foo"))
    }

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(
                next = MatchedInput(
                    FakeInputRepository.googleMapsUriInput,
                    "https://www.google.com/maps/search/39.920439,+116.331538",
                )
            ),
            input.parse("https://www.google.com/maps/search/39.920439,+116.331538"),
        )
    }

    @Test
    fun parse_googleMapsGo_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(
                next = MatchedInput(
                    FakeInputRepository.googleMapsUriInput,
                    @Suppress("SpellCheckingInspection") "https://www.google.com/maps/dir//The+Station,+1+Mends+St,+South+Perth+WA+6151/@-31.9614112,115.8523381,14z/data=!4m6!4m5!1m0!1m2!1m1!1s0x2a32a529928d7447:0x4a1084749ffdee05!3e0!11m1!6b1?entry=ml&utm_campaign=ml-navnp-dr&coh=230964"
                )
            ),
            input.parse("https://maps.app.goo.gl/?utm_campaign=ml-navnp-dr&pcampaignid=ml-navnp-dr&apn=com.google.android.apps.maps&amv=965100030&link=https%3A%2F%2Fwww.google.com%2Fmaps%2Fdir%2F%2FThe%2BStation%2C%2B1%2BMends%2BSt%2C%2BSouth%2BPerth%2BWA%2B6151%2F%40-31.9614112%2C115.8523381%2C14z%2Fdata%3D!4m6!4m5!1m0!1m2!1m1!1s0x2a32a529928d7447%3A0x4a1084749ffdee05!3e0!11m1!6b1%3Fentry%3Dml%26utm_campaign%3Dml-navnp-dr%26coh%3D230964&isi=585027354&ibi=com.google.Maps&ius=comgooglemapsurl&pt=9008&mt=8&ct=ml-navnp-dr&ifl=https%3A%2F%2Fapps.apple.com%2Fapp%2Fid585027354%3Fct%3Dml-navnp-dr%26pt%3D9008%26mt%3D8%26ppid%3Da2beb448-b2c7-4ed7-ae8b-78fdcf9f33a8&efr=1"),
        )
    }
}
