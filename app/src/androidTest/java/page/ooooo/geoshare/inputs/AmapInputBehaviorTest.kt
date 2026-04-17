package page.ooooo.geoshare.inputs

import androidx.test.uiautomator.uiAutomator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import page.ooooo.geoshare.lib.geo.GCJ02Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class AmapInputBehaviorTest : InputBehaviorTest {
    @Test
    fun amap() = uiAutomator {
        // Coordinates within mainland China
        testUri(
            GCJ02Point(
                31.222811749011463, 121.46840706467624,
                name = "上海市黄浦区巨鹿路15-17号",
                source = Source.URI,
            ),
            "https://wb.amap.com/?q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090",
        )

        // Coordinates within Hong Kong
        testUri(
            GCJ02Point(
                22.29196286774012, 114.17731806635854,
                name = @Suppress("SpellCheckingInspection") "Haigangcheng F g",
                source = Source.URI,
            ),
            "https://wb.amap.com/?q=22.29196286774012%2C114.17731806635854%2CHaigangcheng+F+g",
        )

        // Coordinates within Taiwan
        testUri(
            GCJ02Point(
                25.08380369719241, 121.51320397853848,
                name = "台湾省境内",
                source = Source.URI,
            ),
            "https://wb.amap.com/?q=25.08380369719241%2C121.51320397853848%2C%E5%8F%B0%E6%B9%BE%E7%9C%81%E5%A2%83%E5%86%85",
        )

        // Coordinates within western Japan
        testUri(
            WGS84Point(
                34.36875865823159, 131.1821490526199,
                name = "山口县长门市地图选点",
                source = Source.URI,
            ),
            "https://wb.amap.com/?commonBizInfo=%7B%22share_from%22%3A%22com.autonavi.map.search.fragment.SearchCQDetailPage%22%2C%22share_from_type%22%3A%22Native%22%2C%22share_type%22%3A%22url%22%2C%22share_lastClickSpm%22%3A%22amap.27854080.tipBar_RenderPOITipBar.shareBtn%22%2C%22share_bid%22%3A%22tb71dkhi4aoadgfj55md1rmengsknme8f235b8f%22%2C%22share_bizParams%22%3A%22%257B%2522poiid%2522%253A%2522%2522%252C%2522trigger%2522%253A%2522click%2522%252C%2522is_rank%2522%253A0%257D%22%7D&q=34.36875865823159%2C131.1821490526199%2C%E5%B1%B1%E5%8F%A3%E5%8E%BF%E9%95%BF%E9%97%A8%E5%B8%82%E5%9C%B0%E5%9B%BE%E9%80%89%E7%82%B9&src=app_C3090&userRelationToken=e5b28b1e36b011f19f8300163e3c2dbc0",
        )

        // Coordinates within Europe
        testUri(
            WGS84Point(
                45.8289525077221, 1.266689300537103,
                name = @Suppress("SpellCheckingInspection") "利摩日主教座堂,42 Rue Prte Panet, 87000 Limoges, 法国",
                source = Source.URI,
            ),
            "https://wb.amap.com/?p=P0JANYX6NL%2C45.8289525077221%2C1.266689300537103%2C%E5%88%A9%E6%91%A9%E6%97%A5%E4%B8%BB%E6%95%99%E5%BA%A7%E5%A0%82%2C42+Rue+Prte+Panet%2C+87000+Limoges%2C+%E6%B3%95%E5%9B%BD&src=app_C3090",
        )
    }

    @Test
    fun amapHtml() = uiAutomator {
        runBlocking {
            assumeDomainResolvable(@Suppress("SpellCheckingInspection") "surl.amap.com")
        }

        // Short URI
        testUri(
            GCJ02Point(
                31.222811749011463, 121.46840706467624,
                name = "上海市黄浦区巨鹿路15-17号",
                source = Source.URI,
            ),
            "https://surl.amap.com/4mkKGuyJ2bz",
        )
    }
}
