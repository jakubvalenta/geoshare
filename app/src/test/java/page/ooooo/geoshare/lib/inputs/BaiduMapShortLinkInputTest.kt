package page.ooooo.geoshare.lib.inputs

import org.junit.Assert.assertEquals
import org.junit.Test

class BaiduMapShortLinkInputTest : InputTest {
    private val input = BaiduMapShortLinkInput

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
}
