package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.ParseResult

class PermissionDeniedTest {
    private val source = "https://maps.google.com/foo"
    private val input = FakeInputRepository.googleMapsShortLinkInput
    private val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val prevResult = ParseResult(prevPoints)
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_returnsDataParsed() = runTest {
        val state = PermissionDenied(stateContext, source, match = source, input, listOf(prevResult))
        assertEquals(
            DataParsed(
                stateContext, source, match = source, input, Permission.NEVER, listOf(ParseResult(), prevResult)
            ),
            state.transition(),
        )
    }
}
