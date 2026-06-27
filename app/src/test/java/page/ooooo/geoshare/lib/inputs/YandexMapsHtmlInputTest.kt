package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class YandexMapsHtmlInputTest : InputTest {
    private val input = FakeInputRepository.yandexMapsHtmlInput

    @Test
    fun parse_containsCoordinates_returnsPoint() = runTest {
        assertEquals(
            ParseResult(
                persistentListOf(
                    WGS84Point(
                        55.882227, 37.566898,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Keramichesky Drive",
                        source = Source.HTML,
                    )
                )
            ),
            input.parse(
                @Suppress("GrazieInspectionRunner", "SpellCheckingInspection")
                """<meta property="og:image" content="https://static-maps.yandex.ru/1.x/?api_key=XXX&amp;theme=light&amp;lang=en_US&amp;size=520%2C440&amp;l=map&amp;spn=0.012927%2C0.024085&amp;ll=37.563875%2C55.881952&amp;lg=0&amp;cr=0&amp;pt=37.566898%2C55.882227%2Cplacemark&amp;signature=XXX">
                <h1 class="card-title-view__title" itemProp="name">Keramichesky Drive</h1>""".trimIndent()
            ),
        )
    }

    @Test
    fun parse_doesNotContainCoordinates_returnsNoPoints() = runTest {
        assertEquals(ParseResult(), input.parse("""<html></html>"""))
    }
}
