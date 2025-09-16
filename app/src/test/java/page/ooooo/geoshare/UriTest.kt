package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri
import java.net.URL

class UriTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun parse_empty() {
        assertEquals(
            Uri(
                uriQuote = uriQuote,
            ),
            Uri.parse("", uriQuote)
        )
    }

    @Test
    fun parse_pathOnly() {
        assertEquals(
            Uri(
                path = "foo",
                uriQuote = uriQuote,
            ),
            Uri.parse("foo", uriQuote)
        )
    }

    @Test
    fun parse_pathOnlyWithEmptyScheme() {
        assertEquals(
            Uri(
                path = "foo",
                uriQuote = uriQuote,
            ),
            Uri.parse(":foo", uriQuote)
        )
    }

    @Test
    fun parse_queryOnly() {
        assertEquals(
            Uri(
                queryParams = mapOf("baz" to ""),
                uriQuote = uriQuote,
            ),
            Uri.parse("?baz", uriQuote)
        )
    }

    @Test
    fun parse_fragmentOnly() {
        assertEquals(
            Uri(
                fragment = "spam",
                uriQuote = uriQuote,
            ),
            Uri.parse("#spam", uriQuote)
        )
    }

    @Test
    fun parse_hostAndPath() {
        assertEquals(
            Uri(
                host = "foo",
                path = "/bar",
                uriQuote = uriQuote,
            ),
            Uri.parse("foo/bar", uriQuote)
        )
    }

    @Test
    fun parse_hostAndPathAndQuery() {
        assertEquals(
            Uri(
                host = "foo",
                path = "/bar",
                queryParams = mapOf("baz" to ""),
                uriQuote = uriQuote,
            ),
            Uri.parse("foo/bar?baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHost() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndEmptyHost() {
        assertEquals(
            Uri(
                scheme = "https",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndPath() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                path = "/bar",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo/bar", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndPathAndQuery() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                path = "/bar",
                queryParams = mapOf("baz" to ""),
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo/bar?baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndPathAndQueryAndFragment() {
        assertEquals(
            @Suppress("SpellCheckingInspection")
            Uri(
                scheme = "https",
                host = "osmand.net",
                path = "/map",
                queryParams = mapOf("pin" to "52.51628,13.37771"),
                fragment = "12.5/-53.347932/-13.2347",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://osmand.net/map?pin=52.51628,13.37771#12.5/-53.347932/-13.2347", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndQueryWithSlash() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                queryParams = mapOf("bar/baz" to ""),
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo?bar/baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndFragmentWithQuestionMark() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                fragment = "bar?baz",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo#bar?baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndFragmentWithSlash() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                fragment = "bar/baz",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo#bar/baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndEmptyPath() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                path = "/",
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo/", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndHostAndEmptyPathAndQuery() {
        assertEquals(
            Uri(
                scheme = "https",
                host = "foo",
                path = "/",
                queryParams = mapOf("baz" to ""),
                uriQuote = uriQuote,
            ),
            Uri.parse("https://foo/?baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndPath() {
        assertEquals(
            Uri(
                scheme = "geo",
                path = "foo",
                uriQuote = uriQuote,
            ),
            Uri.parse("geo:foo", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndNonhierarchicalHostAndPath() {
        assertEquals(
            Uri(
                scheme = "geo",
                host = "foo",
                path = "/bar",
                uriQuote = uriQuote,
            ),
            Uri.parse("geo:foo/bar", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndPathAndQuery() {
        assertEquals(
            Uri(
                scheme = "geo",
                path = "foo",
                queryParams = mapOf("baz" to ""),
                uriQuote = uriQuote,
            ),
            Uri.parse("geo:foo?baz", uriQuote)
        )
    }

    @Test
    fun parse_schemeAndPathAndFragment() {
        assertEquals(
            Uri(
                scheme = "geo",
                path = "foo",
                fragment = "spam",
                uriQuote = uriQuote,
            ),
            Uri.parse("geo:foo#spam", uriQuote)
        )
    }

    @Test
    fun parse_geoUri() {
        assertEquals(
            Uri(
                scheme = "geo",
                path = "50.123456,-11.123456",
                queryParams = mapOf("q" to "foo bar", "z" to "3.4"),
                uriQuote = uriQuote,
            ),
            Uri.parse("geo:50.123456,-11.123456?q=foo%20bar&z=3.4", uriQuote)
        )
    }

    @Test
    fun parse_queryParams_missingQuery_returnsEmptyMap() {
        assertEquals(
            Uri(queryParams = emptyMap(), uriQuote = uriQuote),
            Uri.parse("?", uriQuote)
        )
    }

    @Test
    fun parse_queryParams_severalParameters_returnsMap() {
        assertEquals(
            Uri(queryParams = mapOf("foo" to "bar", "baz" to "1"), uriQuote = uriQuote),
            Uri.parse("?foo=bar&baz=1", uriQuote)
        )
    }

    @Test
    fun parse_queryParams_urlEncodeParameter_returnsMapWithUrlDecodedValue() {
        assertEquals(
            Uri(queryParams = mapOf("foo" to "bar baz"), uriQuote = uriQuote),
            Uri.parse("?foo=bar%20baz", uriQuote)
        )
    }

    @Test
    fun parse_queryParams_parameterWithoutValue_returnsMapWithEmptyStringsAsTheParameterValue() {
        assertEquals(
            Uri(queryParams = mapOf("foo" to "bar", "spam" to "", "baz" to "1"), uriQuote = uriQuote),
            Uri.parse("?foo=bar&spam&baz=1", uriQuote)
        )
        assertEquals(
            Uri(queryParams = mapOf("foo" to "bar", "spam" to "", "baz" to "1"), uriQuote = uriQuote),
            Uri.parse("?foo=bar&spam=&baz=1", uriQuote)
        )
    }

    @Test
    fun parse_queryParams_parameterWithoutNameOrValue_returnsMapWithEmptyStringsAsTheParameterNameAndValue() {
        assertEquals(
            Uri(queryParams = mapOf("foo" to "bar", "" to "", "baz" to "1"), uriQuote = uriQuote),
            Uri.parse("?foo=bar&=&baz=1", uriQuote)
        )
        assertEquals(
            Uri(queryParams = mapOf("foo" to "bar", "" to "", "baz" to "1"), uriQuote = uriQuote),
            Uri.parse("?foo=bar&&baz=1", uriQuote)
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasSchemeAndHost() {
        assertEquals(
            URL("https://www.example.com/my-path"),
            Uri.parse("https://www.example.com/my-path", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasMissingSchemeAndMissingHostAndRelativePathWithOnePart_returnsUrlWithDefaultSchemeAndHostAndPath() {
        assertEquals(
            URL("https://example.com/default/my-relative"),
            Uri.parse("my-relative", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasMissingSchemeAndMissingHostAndRelativePathWithMultipleParts_returnsUrlWithDefaultSchemeAndHostAndPath() {
        assertEquals(
            URL("https://example.com/default/my-relative/path"),
            Uri.parse("my-relative/path", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasMissingSchemeAndMissingHostAndAbsolutePath_returnsUrlWithDefaultSchemeAndHost() {
        assertEquals(
            URL("https://example.com/my-absolute/path"),
            Uri.parse("/my-absolute/path", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasRelativeScheme_returnsUrlWithDefaultScheme() {
        assertEquals(
            URL("https://my-host.example.com/my-path"),
            Uri.parse("//my-host.example.com/my-path", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasHttpsSchemeAndHost_returnsUrlUnchanged() {
        assertEquals(
            URL("https://my-host"),
            Uri.parse("https://my-host", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasHttpsSchemeAndHostAndPath_returnsUrlUnchanged() {
        assertEquals(
            URL("https://my-host/my-path"),
            Uri.parse("https://my-host/my-path", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
        assertEquals(
            URL("https://my-host/my-long/path"),
            Uri.parse("https://my-host/my-long/path", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlHasFtpSchemeAndHost_returnUrlUnchanged() {
        assertEquals(
            URL("ftp://my-host"),
            Uri.parse("ftp://my-host", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toAbsoluteUrl_inputUrlIsGeoUri_returnHttpsUrl() {
        assertEquals(
            URL("https://example.com/default/1,2"),
            Uri.parse("geo:1,2", uriQuote).toAbsoluteUrl("https", "example.com", "/default")
        )
    }

    @Test
    fun toString_encodesPath() {
        val uriString =
            "https://www.google.com/maps/place/Pozna%C5%84+Old+Town,+61-001+Pozna%C5%84,+Poland/data=12345?utm_source=mstt_1&entry=gps&coh=12345&g_ep=abcd#my-fragment"
        assertEquals(
            uriString,
            Uri.parse(uriString, uriQuote).toString()
        )
    }
}
