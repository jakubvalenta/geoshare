package page.ooooo.geoshare.lib.inputs

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

import android.content.res.Resources
import org.mockito.kotlin.mock

class AppleMapsHtmlInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.appleMapsHtmlInput

    @Test
    fun parse_containsCoordinates_returnsPoint() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(52.4735927, 13.4050798, source = Source.HTML))),
            input.parse(
                // language=html
                """<html lang="en">
<head>
  <meta property="place:location:latitude" content="52.4735927" />
  <meta property="place:location:longitude" content="13.4050798" />
</head>
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
