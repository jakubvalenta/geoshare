package page.ooooo.geoshare.lib.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

class ConversionSucceededTest {
    private val source = "https://maps.google.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = ConversionSucceeded(source, points)
        assertNull(state.transition(stateContext))
    }
}
