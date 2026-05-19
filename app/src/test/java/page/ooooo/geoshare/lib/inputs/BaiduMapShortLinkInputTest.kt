package page.ooooo.geoshare.lib.inputs

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BaiduMapShortLinkInputTest : InputTest {
    private val input = BaiduMapShortLinkInput()

    @Test
    fun match_shortLink() {
        assertEquals("https://j.map.baidu.com/0f/tbWk", input.match("https://j.map.baidu.com/0f/tbWk"))
    }

    @Test
    fun match_shortLinkInText() {
        assertEquals(
            "https://j.map.baidu.com/64/lqEk",
            input.match(
                "这里是地图上的点：江苏省苏州市吴中区金庭镇移影桥\n" +
                    "查看详情>>https://j.map.baidu.com/64/lqEk  #百度地图#"
            ),
        )
    }

    @Test
    fun parse_returnsNextStep() = runTest {
        assertEquals(
            ParseResult(
                nextStep = NextStep(
                    BaiduMapUriInput,
                    "https://map.baidu.com/poi/%E5%9C%B0%E5%9B%BE%E4%B8%8A%E7%9A%84%E7%82%B9/@13392211,3619117,17z"
                )
            ),
            input.parse("https://map.baidu.com/poi/%E5%9C%B0%E5%9B%BE%E4%B8%8A%E7%9A%84%E7%82%B9/@13392211,3619117,17z"),
        )
    }
}
