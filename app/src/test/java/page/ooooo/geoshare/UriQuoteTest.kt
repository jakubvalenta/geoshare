package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote

class UriQuoteTest {
    val uriQuote = FakeUriQuote()

    @Test
    fun encode_encodesSpace() {
        assertEquals("foo%20bar", uriQuote.encode("foo bar"))
    }

    @Test
    fun encode_encodesPlus() {
        @Suppress("SpellCheckingInspection")
        assertEquals("foo%2Bbar", uriQuote.encode("foo+bar"))
    }

    @Test
    fun encode_doesNotEncodeAllowedChars() {
        assertEquals(
            "foo%20bar,baz/spam=spam",
            uriQuote.encode("foo bar,baz/spam=spam", allow = ",/="),
        )
    }
}
