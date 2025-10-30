package page.ooooo.geoshare.lib.converters

import org.junit.Assert.*
import org.junit.Test
import page.ooooo.geoshare.lib.Position

class AmapUrlConverterTest : BaseUrlConverterTest() {
    override val urlConverter = AmapUrlConverter()

    @Test
    fun uriPattern_fullUrlInsideChina() {
        assertTrue(doesUriPatternMatch("https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"))
    }

    @Test
    fun uriPattern_fullUrlOutsideChina() {
        assertTrue(doesUriPatternMatch("https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103%2C%E5%88%A9%E6%91%A9%E6%97%A5%E4%B8%BB%E6%95%99%E5%BA%A7%E5%A0%82%2C42+Rue+Prte+Panet%2C+87000+Limoges%2C+%E6%B3%95%E5%9B%BD&src=app_C3090"))
    }

    @Test
    fun uriPattern_shortUrlInsideChina() {
        assertTrue(doesUriPatternMatch("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun uriPattern_shortUrlOutsideChina() {
        assertTrue(doesUriPatternMatch("https://surl.amap.com/509F4oaxo3QT"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertFalse(doesUriPatternMatch("https://www.example.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertFalse(doesUriPatternMatch("ftp://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"))
    }

    @Test
    fun parseUrl_noPathOrKnownUrlQueryParams() {
        assertNull(parseUrl("https://wb.amap.com"))
        assertNull(parseUrl("https://wb.amap.com/"))
        assertNull(parseUrl("https://wb.amap.com/?spam=1"))
    }

    @Test
    fun parseUrl_insideChina() {
        assertEquals(
            Position("31.224731304675522", "121.46385323166844"),
            parseUrl("https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"),
        )
    }

    @Test
    fun parseUrl_outsideChina() {
        assertEquals(
            Position("45.8289525077221", "1.266689300537103"),
            parseUrl("https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103%2C%E5%88%A9%E6%91%A9%E6%97%A5%E4%B8%BB%E6%95%99%E5%BA%A7%E5%A0%82%2C42+Rue+Prte+Panet%2C+87000+Limoges%2C+%E6%B3%95%E5%9B%BD&src=app_C3090"),
        )
    }

    @Test
    fun isShortUri_correct() {
        assertTrue(isShortUri("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun isShortUri_unknownDomain() {
        assertFalse(isShortUri("https://www.example.com/4mkKGuyJ2bz"))
    }
}
