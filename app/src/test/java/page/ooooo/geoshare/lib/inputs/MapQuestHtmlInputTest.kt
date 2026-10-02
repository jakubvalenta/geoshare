package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class MapQuestHtmlInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.mapQuestHtmlInput

    @Test
    fun match_fullUrl() {
        assertEquals(
            "https://www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097",
            input.match("https://www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097"),
        )
        assertEquals(
            "https://www.mapquest.com/tw/taipei-101-777297285",
            input.match("https://www.mapquest.com/tw/taipei-101-777297285"),
        )
        assertEquals(
            "www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097",
            input.match("www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097")
        )
        assertEquals(
            "mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097",
            input.match("mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097")
        )
    }

    @Test
    fun match_shortLink() {
        assertEquals("https://mapq.st/4hFIEFF", input.match("https://mapq.st/4hFIEFF"))
        assertEquals("mapq.st/4hFIEFF", input.match("mapq.st/4hFIEFF"))
    }

    @Test
    fun match_unknownHost() {
        assertNull(input.match("https://www.example.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097"))
    }

    @Test
    fun parse_success() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    GCJ02MainlandChinaPoint(39.91843414, 116.40497589, source = Source.HTML)
                )
            ),
            input.parse(
                // language=html
                """<html lang="en">
  <body>
    <script>
      {"url":"https://www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097","geo":{"@type":"GeoCoordinates","latitude":39.91843414,"longitude":116.40497589},"address":{"@type":"PostalAddress","addressCountry":"CN","addressLocality":"Beijing","postalCode":"110101"}}
    </script>
  </body>
</html>
"""
            ),
        )
    }

    @Test
    fun parse_failure() = runTest {
        assertEquals(ParseResult.Success(), input.parse("spam"))
    }
}
