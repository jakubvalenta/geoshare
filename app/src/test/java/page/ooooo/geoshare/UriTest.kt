package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Uri

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
}
