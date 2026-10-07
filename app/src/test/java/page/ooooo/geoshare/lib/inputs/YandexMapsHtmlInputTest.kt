package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.network.FakeFetchTools

class YandexMapsHtmlInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.yandexMapsHtmlInput

    @Test
    fun parse_containsCoordinatesInPT_returnsPoint() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(
                        55.882227, 37.566898,
                        name = @Suppress("GrazieInspectionRunner", "SpellCheckingInspection") "Keramichesky Drive",
                        source = Source.HTML,
                    )
                )
            ),
            input.parse(
                "fake match",
                FakeFetchTools(
                    // language=html
                    """
<meta property="og:image" content="https://static-maps.yandex.ru/1.x/?api_key=XXX&amp;theme=light&amp;lang=en_US&amp;size=520%2C440&amp;l=map&amp;spn=0.012927%2C0.024085&amp;ll=37.563875%2C55.881952&amp;lg=0&amp;cr=0&amp;pt=37.566898%2C55.882227%2Cplacemark&amp;signature=XXX">
<h1 class="card-title-view__title" itemProp="name">Keramichesky Drive</h1>
                    """.trimIndent()
                )
            ),
        )
    }

    @Test
    fun parse_containsCoordinatesInLLAndUriDoesNotContainParamLL_returnsPoint() = runTest {
        assertEquals(
            ParseResult.Success(
                persistentListOf(
                    WGS84Point(39.915043, 116.408914, z = 13.0, source = Source.HTML)
                )
            ),
            input.parse(
                "https://yandex.com/maps/org/zapretny_gorod/5867973238",
                FakeFetchTools(
                    // language=html
                    """
<a class="card-footer-view__link" href="https://yandex.com/tune/?retpath=https%3A%2F%2Fyandex.com%2Fmaps%2Forg%2Fzapretny_gorod%2F5867973238%2F%3Fll%3D116.408914%252C39.915043%26z%3D13" target="_blank">Settings</a>
                    """.trimIndent()
                )
            ),
        )
    }

    @Test
    fun parse_containsCoordinatesInLLAndUriDoesNotContainParamLLButHostInLLIsDifferent_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult.Success(),
            input.parse(
                "https://yandex.com/maps/org/zapretny_gorod/5867973238",
                FakeFetchTools(
                    // language=html
                    """
<a class="card-footer-view__link" href="https://yandex.com/tune/?retpath=https%3A%2F%2Fexample.com%2Fmaps%2Forg%2Fzapretny_gorod%2F5867973238%2F%3Fll%3D116.408914%252C39.915043%26z%3D13" target="_blank">Settings</a>
                    """.trimIndent()
                )
            ),
        )
    }

    @Test
    fun parse_containsCoordinatesInLLAndUriContainsParamLL_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult.Success(),
            input.parse(
                "https://yandex.com/maps/org/zapretny_gorod/5867973238/?ll=116.096354%2C40.045755&z=13",
                FakeFetchTools(
                    // language=html
                    """
<a class="card-footer-view__link" href="https://yandex.com/tune/?retpath=https%3A%2F%2Fyandex.com%2Fmaps%2Forg%2Fzapretny_gorod%2F5867973238%2F%3Fll%3D116.408914%252C39.915043%26z%3D13" target="_blank">Settings</a>
                    """.trimIndent()
                )
            ),
        )
    }

    @Test
    fun parse_doesNotContainCoordinates_returnsNoPoints() = runTest {
        assertEquals(
            ParseResult.Success(),
            input.parse(
                "fake match",
                FakeFetchTools(
                    // language=html
                    """<html lang="en"></html>"""
                )
            )
        )
    }
}
