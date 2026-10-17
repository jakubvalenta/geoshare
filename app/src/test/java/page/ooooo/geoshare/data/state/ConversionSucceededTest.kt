package page.ooooo.geoshare.data.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.inputs.MatchedInput

class ConversionSucceededTest {
    private val source = "https://maps.google.com/foo"
    private val matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source)
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_returnsNull() = runTest {
        val state = ConversionSucceeded(source, matchedInput, points)
        assertNull(state.transition(stateContext))
    }
}
