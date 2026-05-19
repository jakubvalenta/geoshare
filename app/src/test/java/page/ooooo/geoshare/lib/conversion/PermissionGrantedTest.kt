package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsWebViewInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class PermissionGrantedTest {
    private val source = "https://maps.google.com/foo"
    private val permission = Permission.ALWAYS
    private val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val prevResult = ParseResult(prevPoints)
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_whenInputIsBasicInput_returnsPermissionGrantedBasicInput() = runTest {
        val input = GoogleMapsHtmlInput(
            googleMapsUriInput = { throw NotImplementedError() },
            googleMapsWebViewInput = { throw NotImplementedError() },
        )
        val state = PermissionGranted(stateContext, source, match = source, input, permission, prevResult)
        assertEquals(
            PermissionGrantedBasicInput(stateContext, source, match = source, input, permission, prevResult),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputIsWebViewInput_returnsPermissionGrantedWebViewInput() = runTest {
        val input = GoogleMapsWebViewInput(
            googleMapsUriInput = { throw NotImplementedError() },
        )
        val state = PermissionGranted(stateContext, source, match = source, input, permission, prevResult)
        assertEquals(
            PermissionGrantedWebViewInput(
                stateContext, source, match = source, input, permission, prevResult
            ),
            state.transition(),
        )
    }
}
