package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source

class MapQuestHtmlInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.mapQuestHtmlInput

    @Test
    fun parse_containsCoordinates_returnsPoint() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    GCJ02MainlandChinaPoint(39.91843414, 116.40497589, source = Source.HTML)
                )
            ),
            input.parse(
                // language=html
                """<html lang="en">
<script>
{"url":"https://www.mapquest.com/cn/manxin-mansion-tiananmen-forbidden-city-beijing-807001097","geo":{"@type":"GeoCoordinates","latitude":39.91843414,"longitude":116.40497589},"address":{"@type":"PostalAddress","addressCountry":"CN","addressLocality":"Beijing","postalCode":"110101"}}
</script>
</html>"""
            ),
        )
    }

    @Test
    fun parse_doesNotContainCoordinates_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult.Success(),
            input.parse(
                // language=html
                """<html lang="en"></html>"""
            )
        )
    }
}
