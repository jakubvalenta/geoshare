package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert
import org.junit.Test
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.point.WGS84Point

class AppleMapsOutputTest {
    private var uriQuote: UriQuote = FakeUriQuote()
    private val output = AppleMapsOutput

    @Test
    fun copyAction_whenUriHasCoordinatesAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            listOf(
                "https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4",
                "https://maps.apple.com/?daddr=50.123456,-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456, z = 3.4)).let { points ->
                output.getPositionActions().map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenUriHasCoordinatesAndQueryAndZoom_returnsCoordinatesAndZoom() {
        Assert.assertEquals(
            listOf(
                "https://maps.apple.com/?ll=50.123456,-11.123456&z=3.4",
                "https://maps.apple.com/?daddr=50.123456,-11.123456",
            ),
            persistentListOf(WGS84Point(50.123456, -11.123456, name = "foo bar", z = 3.4)).let { points ->
                output.getPositionActions().map { it.getText(points, null, uriQuote) }
            },
        )
    }

    @Test
    fun copyAction_whenUriHasQueryAndZoom_returnsQueryAndZoom() {
        Assert.assertEquals(
            listOf(
                "https://maps.apple.com/?q=foo%20bar&z=3.4",
                "https://maps.apple.com/?daddr=foo%20bar",
            ),
            persistentListOf(WGS84Point(name = "foo bar", z = 3.4)).let { points ->
                output.getPositionActions().map { it.getText(points, null, uriQuote) }
            },
        )
    }
}
