package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsWebViewInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.network.RecoverableNetworkException
import page.ooooo.geoshare.lib.network.SocketTimeoutNetworkException
import java.net.SocketTimeoutException

class PermissionGrantedTest {
    @Test
    fun transition_whenInputIsBasicInput_returnsPermissionGrantedBasicInput() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val permission = Permission.ALWAYS
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val cause = SocketTimeoutNetworkException(SocketTimeoutException())
        val lastAttempt = Attempt<RecoverableNetworkException>(1, cause)
        val maxAttempts = 3
        val stateContext: ConversionStateContext = mock()
        val state = PermissionGranted(
            stateContext, source, match = source, input, permission, prevResult, lastAttempt, maxAttempts
        )
        assertEquals(
            PermissionGrantedBasicInput(
                stateContext, source, match = source, input, permission, prevResult, lastAttempt, maxAttempts
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputIsWebViewInput_returnsPermissionGrantedWebViewInput() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsWebViewInput
        val permission = Permission.ALWAYS
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val cause = SocketTimeoutNetworkException(SocketTimeoutException())
        val lastAttempt = Attempt<RecoverableNetworkException>(1, cause)
        val maxAttempts = 3
        val stateContext: ConversionStateContext = mock()
        val state = PermissionGranted(
            stateContext, source, match = source, input, permission, prevResult, lastAttempt, maxAttempts
        )
        assertEquals(
            PermissionGrantedWebViewInput(
                stateContext, source, match = source, input, permission, prevResult
            ),
            state.transition(),
        )
    }
}
