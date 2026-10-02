package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class YahooMapsUriInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.yahooMapsUriInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://map.yahoo.co.jp/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic",
            input.match("https://map.yahoo.co.jp/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic")
        )
        assertEquals(
            "https://map.yahoo.com/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic",
            input.match("https://map.yahoo.com/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic")
        )
        assertEquals(
            "map.yahoo.co.jp/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic",
            input.match("map.yahoo.co.jp/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic")
        )
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic"))
    }

    @Test
    fun match_unknownScheme() {
        assertEquals(
            "map.yahoo.co.jp/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic",
            input.match("ftp://map.yahoo.co.jp/place?lat=35.04038&lon=135.72895&zoom=17&maptype=basic"),
        )
    }

    @Test
    fun match_spaces() {
        assertEquals(
            "https://map.yahoo.co.jp/search?q=foobar",
            input.match("https://map.yahoo.co.jp/search?q=foobar ")
        )
        assertEquals(
            "https://map.yahoo.co.jp/search?q=foo bar",
            input.match("https://map.yahoo.co.jp/search?q=foo bar ")
        )
        assertEquals(
            "https://map.yahoo.co.jp/search?q=foo",
            input.match("https://map.yahoo.co.jp/search?q=foo  bar")
        )
        assertEquals(
            "https://map.yahoo.co.jp/search?q=foo",
            input.match("https://map.yahoo.co.jp/search?q=foo\tbar")
        )
    }

    @Test
    fun parse_unknownPathOrParams() = runTest {
        assertEquals(ParseResult.Success(), input.parse("https://map.yahoo.co.jp"))
        assertEquals(ParseResult.Success(), input.parse("https://map.yahoo.co.jp/"))
        assertEquals(ParseResult.Success(), input.parse("https://map.yahoo.co.jp/search"))
    }

    @Test
    fun parse_mapCenter() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(35.04067, 135.73777, z = 16.0, source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://map.yahoo.co.jp/?lat=35.04067&lon=135.73777&zoom=16&maptype=basic"),
        )
    }

    @Test
    fun parse_placeWithId() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(35.04000, 135.72805, z = 17.0, source = Source.MAP_CENTER)
                )
            ),
            input.parse("https://map.yahoo.co.jp/place?gid=xnjNltgGwY6&lat=35.04000&lon=135.72805&zoom=17&maptype=basic"),
        )
    }

    @Test
    fun parse_placeWithoutId() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(35.05083, 135.76469, z = 17.0, source = Source.URI)
                )
            ),
            input.parse("https://map.yahoo.co.jp/place?lat=35.05083&lon=135.76469&zoom=17&maptype=basic"),
        )
    }

    @Test
    fun parse_search() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.03935, 135.72926,
                        z = 17.0,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Kinkaku-ji",
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://map.yahoo.co.jp/search?q=Kinkaku-ji&lat=35.03935&lon=135.72926&zoom=17&maptype=basic"),
        )
    }

    @Test
    fun parse_searchQueryOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(name = "foo bar", source = Source.URI)
                )
            ),
            input.parse("https://map.yahoo.co.jp/search?q=foo%20bar"),
        )
    }

    @Test
    fun parse_searchQueryOnlyBlank() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://map.yahoo.co.jp/search?q="),
        )
        assertEquals(
            ParseResult.Success(persistentListOf()),
            input.parse("https://map.yahoo.co.jp/search?q=%20"),
        )
    }

    @Test
    fun parse_searchWithoutCoordinates() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Kinkaku-ji",
                        source = Source.URI,
                    )
                )
            ),
            input.parse("https://map.yahoo.co.jp/search?q=Kinkaku-ji&zoom=17"),
        )
    }

    @Test
    fun parse_directionsOriginOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.04361, 135.75926,
                        z = 14.0,
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/walk?fromLat=35.04361&fromLon=135.75926&sort=1&lat=35.02766&lon=135.76411&zoom=14&maptype=basic")
        )
    }

    @Test
    fun parse_directionsDestinationOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.01084, 135.75962,
                        z = 14.0,
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/walk?toLat=35.01084&toLon=135.75962&sort=1&lat=35.02766&lon=135.76411&zoom=14&maptype=basic")
        )
    }

    @Test
    fun parse_directionsOriginAndWaypointsAndDestination() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.04361, 135.75926,
                        z = 14.0,
                        name = "京都府京都市北区小山上総町",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        35.02924, 135.76683,
                        z = 14.0,
                        name = "京都府京都市上京区革堂内町",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        35.01749, 135.76726,
                        z = 14.0,
                        name = "京都府京都市中京区下御霊前町",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        35.01084, 135.75962,
                        z = 14.0,
                        name = "京都府京都市中京区虎屋町",
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/walk?from=%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E5%8C%97%E5%8C%BA%E5%B0%8F%E5%B1%B1%E4%B8%8A%E7%B7%8F%E7%94%BA&to=%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%AD%E4%BA%AC%E5%8C%BA%E8%99%8E%E5%B1%8B%E7%94%BA&fromLat=35.04361&fromLon=135.75926&toLat=35.01084&toLon=135.75962&waypoints=name%3A%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%8A%E4%BA%AC%E5%8C%BA%E9%9D%A9%E5%A0%82%E5%86%85%E7%94%BA%2Clat%3A35.02924%2Clon%3A135.76683%3Bname%3A%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%AD%E4%BA%AC%E5%8C%BA%E4%B8%8B%E5%BE%A1%E9%9C%8A%E5%89%8D%E7%94%BA%2Clat%3A35.01749%2Clon%3A135.76726%3B&sort=1&lat=35.02766&lon=135.76411&zoom=14&maptype=basic")
        )
    }

    @Test
    fun parse_directionsNamesOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        name = "京都府京都市北区小山上総町",
                        z = 14.0,
                        source = Source.URI,
                    ),
                    WGS84Point(
                        name = "京都府京都市上京区革堂内町",
                        z = 14.0,
                        source = Source.URI,
                    ),
                    WGS84Point(
                        name = "京都府京都市中京区下御霊前町",
                        z = 14.0,
                        source = Source.URI,
                    ),
                    WGS84Point(
                        name = "京都府京都市中京区虎屋町",
                        z = 14.0,
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/walk?from=%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E5%8C%97%E5%8C%BA%E5%B0%8F%E5%B1%B1%E4%B8%8A%E7%B7%8F%E7%94%BA&to=%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%AD%E4%BA%AC%E5%8C%BA%E8%99%8E%E5%B1%8B%E7%94%BA&waypoints=name%3A%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%8A%E4%BA%AC%E5%8C%BA%E9%9D%A9%E5%A0%82%E5%86%85%E7%94%BA%3Bname%3A%E4%BA%AC%E9%83%BD%E5%BA%9C%E4%BA%AC%E9%83%BD%E5%B8%82%E4%B8%AD%E4%BA%AC%E5%8C%BA%E4%B8%8B%E5%BE%A1%E9%9C%8A%E5%89%8D%E7%94%BA%3B&sort=1&lat=35.02766&lon=135.76411&zoom=14&maptype=basic")
        )
    }

    @Test
    fun parse_directionsInvalidWaypoints() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.04361, 135.75926,
                        source = Source.URI,
                    ),
                    WGS84Point(
                        name = "missing value of 'lat'",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        name = "unknown key 'spam'",
                        source = Source.URI,
                    ),
                    WGS84Point(
                        35.01084, 135.75962,
                        source = Source.URI,
                    ),
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/walk?fromLat=35.04361&fromLon=135.75926&toLat=35.01084&toLon=135.75962&waypoints=name%3Amissing%20value%20of%20'lat'%2Clat%3Bname%3Aunknown%20key%20'spam'%2Cspam%3A%3B")
        )
    }

    @Test
    fun parse_directionsMapCenterOnly() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.03935, 135.72926,
                        z = 17.0,
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/train?lat=35.03935&lon=135.72926&zoom=17&maptype=basic")
        )
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        35.02766, 135.76411,
                        z = 14.0,
                        source = Source.MAP_CENTER,
                    )
                )
            ),
            input.parse("https://map.yahoo.co.jp/route/car?lat=35.02766&lon=135.76411&zoom=14&maptype=basic")
        )
    }
}
