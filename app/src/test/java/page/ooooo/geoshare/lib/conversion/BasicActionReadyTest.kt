package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.NoopAction

class BasicActionReadyTest {
    private val source = "https://maps.google.com/foo"
    private val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val action = NoopAction

    @Test
    fun transition_returnsNull() = runTest {
        val state = BasicActionReady(source, points, action, isAutomation = true)
        assertNull(state.transition())
    }
}
