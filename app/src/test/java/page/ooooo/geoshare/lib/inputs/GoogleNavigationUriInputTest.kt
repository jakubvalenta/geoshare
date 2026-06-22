package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class GoogleNavigationUriInputTest : InputTest {
    private val input = FakeInputRepository.googleNavigationUriInput

    @Test
    fun match_valid() {
        assertEquals(
            "google.navigation:q=50.123456,-120.123456",
            input.match("google.navigation:q=50.123456,-120.123456")
        )
        assertEquals(
            "google.navigation:q=50.123456+-120.123456",
            input.match("google.navigation:q=50.123456+-120.123456")
        )
        assertEquals(
            "google.navigation:q=foo+bar",
            input.match("google.navigation:q=foo+bar")
        )
        assertEquals(
            "google.navigation:q=foo+bar&avoid=tf&model=l",
            input.match("google.navigation:q=foo+bar&avoid=tf&model=l")
        )
        assertEquals(
            "google.navigation:avoid=tf&model=l&q=foo+bar",
            input.match("google.navigation:avoid=tf&model=l&q=foo+bar")
        )
    }

    @Test
    fun match_noPath() {
        assertNull(input.match("google.navigation:"))
    }

    @Test
    fun match_spaces() = runTest {
        assertEquals(
            "google.navigation:q=foobar",
            input.match("google.navigation:q=foobar ")
        )
        assertEquals(
            "google.navigation:q=foo bar",
            input.match("google.navigation:q=foo bar ")
        )
        assertEquals(
            "google.navigation:q=foo",
            input.match("google.navigation:q=foo  bar")
        )
        assertEquals(
            "google.navigation:q=foo",
            input.match("google.navigation:q=foo\tbar")
        )
    }

    @Test
    fun parse_qWithCoordinatesSeparatedByComma() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(50.123456, -120.123456, source = Source.URI))
            ),
            input.parse("google.navigation:q=50.123456,-120.123456")
        )
    }

    @Test
    fun parse_qWithCoordinatesSeparatedByPlus() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(50.123456, -120.123456, source = Source.URI))
            ),
            input.parse("google.navigation:q=50.123456%2B-120.123456")
        )
    }

    @Test
    fun parse_qWithCoordinatesSeparatedBySpace() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(50.123456, -120.123456, source = Source.URI))
            ),
            input.parse("google.navigation:q=50.123456%20-120.123456")
        )
    }

    @Test
    fun parse_qWithSearch() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "foo bar", source = Source.URI)),
                next = MatchedInput(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://maps.google.com?q=foo%20bar"
                )
            ),
            input.parse("google.navigation:q=foo+bar")
        )
    }

    @Test
    fun parse_qAndAvoidAndModel() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "foo bar", source = Source.URI)),
                next = MatchedInput(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://maps.google.com?q=foo%20bar"
                )
            ),
            input.parse("google.navigation:q=foo+bar&avoid=tf&model=l")
        )
    }

    @Test
    fun parse_qNotAtStart() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(GCJ02MainlandChinaPoint(name = "foo bar", source = Source.URI)),
                next = MatchedInput(
                    FakeInputRepository.googleMapsAddressApiInput,
                    "https://maps.google.com?q=foo%20bar"
                )
            ),
            input.parse("google.navigation:avoid=tf&model=l&q=foo+bar")
        )
    }
}
