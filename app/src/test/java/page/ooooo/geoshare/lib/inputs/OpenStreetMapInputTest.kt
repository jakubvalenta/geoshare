package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.point.WGS84Point

class OpenStreetMapInputTest : BaseInputTest() {
    override val input = OpenStreetMapInput

    @Test
    fun uriPattern_fullUrl() {
        assertTrue(doesUriPatternMatch("https://www.openstreetmap.org/#map=16/51.49/-0.13"))
        assertTrue(doesUriPatternMatch("www.openstreetmap.org/#map=16/51.49/-0.13"))
        assertTrue(doesUriPatternMatch("openstreetmap.org/#map=16/51.49/-0.13"))
        assertTrue(doesUriPatternMatch("https://www.openstreetmap.org/directions?to=51.0528,13.7364"))
        assertTrue(doesUriPatternMatch("https://www.openstreetmap.org/node/6284640534"))
        assertTrue(doesUriPatternMatch("https://www.openstreetmap.org/relation/910699"))
        assertTrue(doesUriPatternMatch("https://www.openstreetmap.org/way/596674456"))
        assertTrue(doesUriPatternMatch("https://osm.org/#map=16/51.49/-0.13"))
    }

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://osm.org/go/0EEQjE--"))
        assertTrue(doesUriPatternMatch("https://openstreetmap.org/go/0EEQjE--"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/#map=16/51.49/-0.13"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "www.openstreetmap.org/#map=16/51.49/-0.13",
            input.uriPattern.find("ftp://www.openstreetmap.org/#map=16/51.49/-0.13")?.value,
        )
    }

    @Test
    fun parseUri_coordinates() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.49, -0.13, z = 16.0))),
            parseUri("https://www.openstreetmap.org/#map=16/51.49/-0.13"),
        )
    }

    @Test
    fun parseUri_coordinatesEncoded() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.49, -0.13, z = 16.0))),
            parseUri("https://www.openstreetmap.org/#map%3D16%2F51.49%2F-0.13"),
        )
    }

    @Test
    fun parseUri_directions() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.0528, 13.7364))),
            parseUri("https://www.openstreetmap.org/directions?to=51.0528,13.7364"),
        )
    }

    @Test
    fun parseUri_element() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(), "https://www.openstreetmap.org/api/0.6/node/6284640534.json"
            ),
            parseUri("https://www.openstreetmap.org/node/6284640534"),
        )
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(), "https://www.openstreetmap.org/api/0.6/relation/910699/full.json"
            ),
            parseUri("https://www.openstreetmap.org/relation/910699"),
        )
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(
                persistentListOf(), "https://www.openstreetmap.org/api/0.6/way/596674456/full.json"
            ),
            parseUri("https://www.openstreetmap.org/way/596674456"),
        )
    }

    @Test
    fun parseUri_shortLink() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.510772705078125, 0.054931640625, z = 9.0))),
            parseUri("https://osm.org/go/0EEQjE--"),
        )
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(51.510772705078125, 0.054931640625, z = 9.0))),
            parseUri("https://openstreetmap.org/go/0EEQjE--"),
        )
    }

    @Test
    fun parseUri_shortLinkNegative() = runTest {
        assertEquals(
            ParseUriResult.Succeeded(persistentListOf(WGS84Point(-16.23152732849121, -49.08348083496094, z = 11.0))),
            parseUri("https://osm.org/go/NuJWxJh-"),
        )
    }

    @Test
    fun parseHtml_node() = runTest {
        val json =
            """{"version":"0.6","elements":[{"type":"node","id":6284640534,"lat":45.4771659,"lon":9.2297918,"timestamp":"2024-03-07T19:04:58Z"}]}"""
        assertEquals(
            ParseHtmlResult.Succeeded(persistentListOf(WGS84Point(45.4771659, 9.2297918))),
            parseHtml(json),
        )
    }

    @Test
    fun parseHtml_relation() = runTest {
        val json =
            """{"version":"0.6","elements":[{"type":"node","id":259609295,"lat":45.4776025,"lon":9.2297852,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":259609297,"lat":45.4773399,"lon":9.2296095,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":259609299,"lat":45.4770943,"lon":9.2295887,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":259609300,"lat":45.4770881,"lon":9.2292100,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":259609301,"lat":45.4772588,"lon":9.2292121,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":672396197,"lat":45.4776002,"lon":9.2295189,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":672396203,"lat":45.4776002,"lon":9.2293737,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747566,"lat":45.4773805,"lon":9.2292100,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747569,"lat":45.4774762,"lon":9.2292100,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747572,"lat":45.4774959,"lon":9.2295256,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747575,"lat":45.4774770,"lon":9.2295534,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747579,"lat":45.4776323,"lon":9.2295472,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747585,"lat":45.4773177,"lon":9.2295833,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747586,"lat":45.4776132,"lon":9.2296902,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747589,"lat":45.4773774,"lon":9.2295549,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747592,"lat":45.4774959,"lon":9.2293841,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747593,"lat":45.4776040,"lon":9.2296902,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747596,"lat":45.4773606,"lon":9.2295265,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747598,"lat":45.4772779,"lon":9.2291696,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747599,"lat":45.4774817,"lon":9.2293589,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747600,"lat":45.4773591,"lon":9.2293890,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747601,"lat":45.4773621,"lon":9.2291674,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":730747602,"lat":45.4773774,"lon":9.2293573,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":3183380568,"lat":45.4775994,"lon":9.2290779,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":3183380573,"lat":45.4776882,"lon":9.2292067,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":3183380576,"lat":45.4775657,"lon":9.2290790,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":3183380593,"lat":45.4776890,"lon":9.2292514,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":4612338079,"lat":45.4772297,"lon":9.2296848,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":4612338080,"lat":45.4773415,"lon":9.2296804,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":4612338081,"lat":45.4772313,"lon":9.2297776,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":5683348856,"lat":45.4775818,"lon":9.2290785,"timestamp":"2025-06-26T14:23:42Z"},{"type":"node","id":5683443074,"lat":45.4776002,"lon":9.2294250,"timestamp":"2025-06-26T14:23:42Z"},{"type":"node","id":5683443076,"lat":45.4773300,"lon":9.2295985,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":5683443077,"lat":45.4773698,"lon":9.2295407,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":5683443083,"lat":45.4774878,"lon":9.2293679,"timestamp":"2024-12-04T11:40:14Z"},{"type":"node","id":9568174710,"lat":45.4772596,"lon":9.2291990,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":9568174711,"lat":45.4773797,"lon":9.2291925,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":9568174712,"lat":45.4776124,"lon":9.2295200,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":9568174713,"lat":45.4776315,"lon":9.2296597,"timestamp":"2022-03-10T09:33:31Z"},{"type":"node","id":12954188903,"lat":45.4774366,"lon":9.2292100,"timestamp":"2025-06-26T14:23:42Z"},{"type":"way","id":23949381,"timestamp":"2025-06-26T14:23:42Z"},{"type":"way","id":58968321,"timestamp":"2018-06-11T23:19:40Z"},{"type":"relation","id":910699,"timestamp":"2019-04-30T22:22:34Z"},{"type":"way","ref":58968321,"role":"inner"}],}]}"""
        assertEquals(
            ParseHtmlResult.Succeeded(
                persistentListOf(
                    WGS84Point(45.4776025, 9.2297852),
                    WGS84Point(45.4773399, 9.2296095),
                    WGS84Point(45.4770943, 9.2295887),
                    WGS84Point(45.4770881, 9.2292100),
                    WGS84Point(45.4772588, 9.2292121),
                    WGS84Point(45.4776002, 9.2295189),
                    WGS84Point(45.4776002, 9.2293737),
                    WGS84Point(45.4773805, 9.2292100),
                    WGS84Point(45.4774762, 9.2292100),
                    WGS84Point(45.4774959, 9.2295256),
                    WGS84Point(45.4774770, 9.2295534),
                    WGS84Point(45.4776323, 9.2295472),
                    WGS84Point(45.4773177, 9.2295833),
                    WGS84Point(45.4776132, 9.2296902),
                    WGS84Point(45.4773774, 9.2295549),
                    WGS84Point(45.4774959, 9.2293841),
                    WGS84Point(45.4776040, 9.2296902),
                    WGS84Point(45.4773606, 9.2295265),
                    WGS84Point(45.4772779, 9.2291696),
                    WGS84Point(45.4774817, 9.2293589),
                    WGS84Point(45.4773591, 9.2293890),
                    WGS84Point(45.4773621, 9.2291674),
                    WGS84Point(45.4773774, 9.2293573),
                    WGS84Point(45.4775994, 9.2290779),
                    WGS84Point(45.4776882, 9.2292067),
                    WGS84Point(45.4775657, 9.2290790),
                    WGS84Point(45.4776890, 9.2292514),
                    WGS84Point(45.4772297, 9.2296848),
                    WGS84Point(45.4773415, 9.2296804),
                    WGS84Point(45.4772313, 9.2297776),
                    WGS84Point(45.4775818, 9.2290785),
                    WGS84Point(45.4776002, 9.2294250),
                    WGS84Point(45.4773300, 9.2295985),
                    WGS84Point(45.4773698, 9.2295407),
                    WGS84Point(45.4774878, 9.2293679),
                    WGS84Point(45.4772596, 9.2291990),
                    WGS84Point(45.4773797, 9.2291925),
                    WGS84Point(45.4776124, 9.2295200),
                    WGS84Point(45.4776315, 9.2296597),
                    WGS84Point(45.4774366, 9.2292100),
                )
            ),
            parseHtml(json),
        )
    }

    @Test
    fun parseHtml_way() = runTest {
        val json =
            """{"version":"0.6","elements":[{"type":"node","id":5683443079,"lat":45.4770640,"lon":9.2296749,"timestamp":"2023-09-18T20:49:59Z"},{"type":"node","id":5683443080,"lat":45.4771158,"lon":9.2296737,"timestamp":"2023-03-03T21:48:57Z"},{"type":"node","id":5683443081,"lat":45.4771159,"lon":9.2296361,"timestamp":"2018-06-11T23:19:39Z"},{"type":"node","id":5683443082,"lat":45.4772950,"lon":9.2296354,"timestamp":"2018-06-11T23:19:39Z"},{"type":"way","id":596674456,"timestamp":"2025-06-26T19:35:22Z","nodes":[5683443079,5683443080,5683443081,5683443082]}]}"""
        assertEquals(
            ParseHtmlResult.Succeeded(
                persistentListOf(
                    WGS84Point(45.4770640, 9.2296749),
                    WGS84Point(45.4771158, 9.2296737),
                    WGS84Point(45.4771159, 9.2296361),
                    WGS84Point(45.4772950, 9.2296354),
                )
            ),
            parseHtml(json),
        )
    }
}
