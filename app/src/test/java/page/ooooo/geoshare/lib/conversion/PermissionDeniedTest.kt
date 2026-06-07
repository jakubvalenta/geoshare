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
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class PermissionDeniedTest {
    private val source = "https://maps.google.com/foo"
    private val input = FakeInputRepository.googleMapsShortLinkInput
    private val matchedInput = MatchedInput(input, source)
    private val oldPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val oldResult = ParseResult(oldPoints)
    private val results: Results = mapOf(MatchedInput(FakeInputRepository.debugUriInput, source) to oldResult)
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_returnsDataParsed() = runTest {
        val state = PermissionDenied(stateContext, source, matchedInput, results)
        assertEquals(
            DataParsed(
                stateContext, source, matchedInput, Permission.NEVER, results + (matchedInput to ParseResult())
            ),
            state.transition(),
        )
    }
}
