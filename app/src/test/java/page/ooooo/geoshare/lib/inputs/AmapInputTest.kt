package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02GreaterChinaAndTaiwanPoint
import page.ooooo.geoshare.lib.geo.Source

class AmapInputTest : InputTest {
    override val input = AmapInput

    @Test
    fun uriPattern_fullUrlWithinMainlandChina() {
        assertEquals(
            "https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090",
            getUri("https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090")
        )
    }

    @Test
    fun uriPattern_fullUrlOutsideMainlandChina() {
        assertEquals(
            "https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103%2C%E5%88%A9%E6%91%A9%E6%97%A5%E4%B8%BB%E6%95%99%E5%BA%A7%E5%A0%82%2C42+Rue+Prte+Panet%2C+87000+Limoges%2C+%E6%B3%95%E5%9B%BD&src=app_C3090",
            getUri("https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103%2C%E5%88%A9%E6%91%A9%E6%97%A5%E4%B8%BB%E6%95%99%E5%BA%A7%E5%A0%82%2C42+Rue+Prte+Panet%2C+87000+Limoges%2C+%E6%B3%95%E5%9B%BD&src=app_C3090")
        )
    }

    @Test
    fun uriPattern_shortUrlWithinMainlandChina() {
        assertEquals("https://surl.amap.com/4mkKGuyJ2bz", getUri("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun uriPattern_shortUrlOutsideMainlandChina() {
        assertEquals("https://surl.amap.com/509F4oaxo3QT", getUri("https://surl.amap.com/509F4oaxo3QT"))
    }

    @Test
    fun uriPattern_unknownHost() {
        assertNull(getUri("https://www.example.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"))
    }

    @Test
    fun uriPattern_unknownScheme() {
        assertEquals(
            "wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090",
            getUri("ftp://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"),
        )
    }

    @Test
    fun parseUri_noPathOrKnownUrlQueryParams() = runTest {
        assertEquals(ParseUriResult(), parseUri("https://wb.amap.com"))
        assertEquals(ParseUriResult(), parseUri("https://wb.amap.com/"))
        assertEquals(ParseUriResult(), parseUri("https://wb.amap.com/?spam=1"))
    }

    @Test
    fun parseUri_desktop() = runTest {
        // TODO Add support for Amap desktop URLs
        assumeTrue(false)
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02GreaterChinaAndTaiwanPoint(
                        31.222811749011463, 121.46840706467624,
                        name = "上海市黄浦区巨鹿路15-17号",
                        source = Source.URI,
                    ),
                )
            ),
            parseUri("https://www.amap.com/regeo?lng=121.46840706467624&lat=31.222811749011463&name=%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&adcode"),
        )
    }

    @Test
    fun parseUri_qParamWithName() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02GreaterChinaAndTaiwanPoint(
                        31.222811749011463, 121.46840706467624,
                        name = "上海市黄浦区巨鹿路15-17号",
                        source = Source.URI,
                    ),
                )
            ),
            parseUri("https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090"),
        )
    }

    @Test
    fun parseUri_qParamWithoutName() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02GreaterChinaAndTaiwanPoint(
                        31.222811749011463,
                        121.46840706467624,
                        source = Source.URI
                    )
                )
            ),
            parseUri("https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624"),
        )
    }

    @Test
    fun parseUri_qParamWithOtherParams() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02GreaterChinaAndTaiwanPoint(
                        34.36875865823159, 131.1821490526199,
                        name = "山口县长门市地图选点",
                        source = Source.URI,
                    ),
                )
            ),
            parseUri("https://wb.amap.com/?commonBizInfo=%7B%22share_from%22%3A%22com.autonavi.map.search.fragment.SearchCQDetailPage%22%2C%22share_from_type%22%3A%22Native%22%2C%22share_type%22%3A%22url%22%2C%22share_lastClickSpm%22%3A%22amap.27854080.tipBar_RenderPOITipBar.shareBtn%22%2C%22share_bid%22%3A%22tb71dkhi4aoadgfj55md1rmengsknme8f235b8f%22%2C%22share_bizParams%22%3A%22%257B%2522poiid%2522%253A%2522%2522%252C%2522trigger%2522%253A%2522click%2522%252C%2522is_rank%2522%253A0%257D%22%7D&q=34.36875865823159%2C131.1821490526199%2C%E5%B1%B1%E5%8F%A3%E5%8E%BF%E9%95%BF%E9%97%A8%E5%B8%82%E5%9C%B0%E5%9B%BE%E9%80%89%E7%82%B9&src=app_C3090&userRelationToken=e5b28b1e36b011f19f8300163e3c2dbc0"),
        )
    }

    @Test
    fun parseUri_pParamWithName() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02GreaterChinaAndTaiwanPoint(
                        45.8289525077221, 1.266689300537103,
                        name = @Suppress("SpellCheckingInspection") "利摩日主教座堂,42 Rue Prte Panet, 87000 Limoges, 法国",
                        source = Source.URI,
                    ),
                )
            ),
            parseUri("https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103%2C%E5%88%A9%E6%91%A9%E6%97%A5%E4%B8%BB%E6%95%99%E5%BA%A7%E5%A0%82%2C42+Rue+Prte+Panet%2C+87000+Limoges%2C+%E6%B3%95%E5%9B%BD&src=app_C3090"),
        )
    }

    @Test
    fun parseUri_pParamWithoutName() = runTest {
        assertEquals(
            ParseUriResult(
                persistentListOf(
                    GCJ02GreaterChinaAndTaiwanPoint(
                        45.8289525077221,
                        1.266689300537103,
                        source = Source.URI
                    )
                )
            ),
            parseUri("https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103"),
        )
    }

    @Test
    fun shortUriPattern_correct() {
        assertNotNull(getShortUri("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun shortUriPattern_unknownDomain() {
        assertNull(getShortUri("https://www.example.com/4mkKGuyJ2bz"))
    }
}
