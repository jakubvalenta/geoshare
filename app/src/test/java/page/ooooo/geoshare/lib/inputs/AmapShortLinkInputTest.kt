package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository

class AmapShortLinkInputTest : InputTest {
    private val input = FakeInputRepository.amapShortLinkInput

    @Test
    fun match() {
        assertEquals("https://surl.amap.com/4mkKGuyJ2bz", input.match("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun match_correct() {
        assertEquals("https://surl.amap.com/4mkKGuyJ2bz", input.match("https://surl.amap.com/4mkKGuyJ2bz"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/4mkKGuyJ2bz"))
    }

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    FakeInputRepository.amapUriInput,
                    "https://wb.amap.com/?commonBizInfo=%7B%22share_from%22%3A%22com.autonavi.map.search.fragment.SearchCQDetailPage%22,%22share_from_type%22%3A%22Native%22,%22share_type%22%3A%22url%22,%22share_lastClickSpm%22%3A%22amap.27854080.tipBar_RenderPOITipBar.shareBtn%22,%22share_bid%22%3A%2210tbdkhiacjdg8fq55drmmengskmkev8f235b8f%22,%22share_bizParams%22%3A%22%257B%2522poiid%2522%253A%2522%2522%252C%2522trigger%2522%253A%2522click%2522%257D%22%7D&q=31.222811749011463,121.46840706467624,%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090&userRelationToken=a3175f02b59811f08caa00163e0364fe0"
                )
            ),
            input.parse("https://wb.amap.com/?commonBizInfo=%7B%22share_from%22%3A%22com.autonavi.map.search.fragment.SearchCQDetailPage%22%2C%22share_from_type%22%3A%22Native%22%2C%22share_type%22%3A%22url%22%2C%22share_lastClickSpm%22%3A%22amap.27854080.tipBar_RenderPOITipBar.shareBtn%22%2C%22share_bid%22%3A%2210tbdkhiacjdg8fq55drmmengskmkev8f235b8f%22%2C%22share_bizParams%22%3A%22%257B%2522poiid%2522%253A%2522%2522%252C%2522trigger%2522%253A%2522click%2522%257D%22%7D&q=31.222811749011463%2C121.46840706467624%2C%E4%B8%8A%E6%B5%B7%E5%B8%82%E9%BB%84%E6%B5%A6%E5%8C%BA%E5%B7%A8%E9%B9%BF%E8%B7%AF15-17%E5%8F%B7&src=app_C3090&userRelationToken=a3175f02b59811f08caa00163e0364fe0"),
        )
    }
}
