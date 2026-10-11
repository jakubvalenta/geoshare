package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.network.FakeFetchTools

class AppleMapsHtmlInputTest : InputTest {
    override val resources: Resources = mock()
    private val input = FakeInputRepository.appleMapsHtmlInput

    @Test
    fun parse_containsCoordinates_returnsPoint() = runTest {
        assertEquals(
            ParseResult.Success(persistentListOf(WGS84Point(52.4735927, 13.4050798, source = Source.HTML))),
            input.parse(
                "fake match",
                FakeFetchTools(
                    // language=html
                    """<html lang="en">
<head>
  <meta property="place:location:latitude" content="52.4735927" />
  <meta property="place:location:longitude" content="13.4050798" />
</head>
</html>"""
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
