package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs

class BaiduMapInputTest : BaseInputTest() {
    override val input = BaiduMapInput

    @Test
    fun uriPattern_shortUrl() {
        assertTrue(doesUriPatternMatch("https://j.map.baidu.com/0f/tbWk"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/0f/tbWk"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://j.map.baidu.com/0f/tbWk"))
    }

    @Test
    fun parseUri_whenUriStringIsAnyString_returnsSucceededAndSupportsHtmlParsing() = runTest {
        assertEquals(
            ParseUriResult.SucceededAndSupportsHtmlParsing(Position(Srs.GCJ02), "foo"),
            parseUri("foo"),
        )
    }

    @Test
    fun parseHtml_containsCoordinatesInBeijing_returnsPosition() = runTest {
        assertEquals(
            ParseHtmlResult.Succeeded(Position(Srs.GCJ02, 30.940779294367676, 118.75866025417945)),
            @Suppress("SpellCheckingInspection")
            parseHtml(
                """window._OLR = {"index":"{\"content\":{\"area\":190,\"baike\":0,\"city_type\":2,\"cname\":\"\\u5ba3\\u57ce\\u5e02\",\"code\":190,\"count_info\":{\"groupon\":26,\"premium\":0},\"ext\":[],\"geo\":\"1|13221024.95,3603638.53;13221024.95,3603638.53|13221024.95,3603638.53;\",\"if_current\":1,"""
            )
        )
        assertEquals(
            ParseHtmlResult.Succeeded(Position(Srs.GCJ02, 22.542860065931386, 114.05956013578918)),
            @Suppress("SpellCheckingInspection")
            parseHtml(
                """window._OLR = {"index":"{\"content\":{\"area\":340,\"baike\":0,\"city_type\":2,\"cname\":\"\\u6df1\\u5733\\u5e02\",\"code\":340,\"count_info\":{\"groupon\":21952,\"premium\":2},\"ext\":[],\"geo\":\"1|12697919.69,2560977.31;12697919.69,2560977.31|12697919.69,2560977.31;\",\"if_current\":1,"""
            )
        )
    }

    @Test
    fun parseHtml_doesNotContainCoordinates_returnsNull() = runTest {
        assertNull(parseHtml("""<html></html>"""))
    }
}
