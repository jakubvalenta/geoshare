package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import android.net.Uri
import org.mockito.Mockito
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.Position
import kotlin.jvm.java

class PositionTest {
    private val uriQuote = FakeUriQuote()

    @Test
    fun position_fromGeoUri_returnsAllCoordsAndParams() {
        val mockUri = Mockito.mock(Uri::class.java)
        Mockito.`when`(mockUri.scheme).thenReturn("geo")
        Mockito.`when`(mockUri.authority).thenReturn("50.123456,11.123456")
        Mockito.`when`(mockUri.query).thenReturn("q=foo%20bar&z=3")
        assertEquals(
            Position(
                Position.Coords("50.123456", "11.123456"),
                Position.Params(q = "foo bar", z = "3"),
            ),
            Position.fromGeoUri(mockUri, uriQuote),
        )
    }
}
