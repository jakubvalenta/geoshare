package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.NoopInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput

class PermissionGrantedTest {
    private val source = "https://maps.google.com/foo"
    private val permission = Permission.ALWAYS
    private val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val prevResult = ParseResult(prevPoints)
    private val stateContext: ConversionStateContext = mock()

    @Test
    fun transition_whenInputIsBasicInput_returnsPermissionGrantedBasicInput() = runTest {
        val input = FakeInputRepository.googleMapsShortLinkInput
        val state = PermissionGranted(stateContext, source, match = source, input, permission, listOf(prevResult))
        assertEquals(
            PermissionGrantedBasicInput(stateContext, source, match = source, input, permission, listOf(prevResult)),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputIsWebViewInput_returnsPermissionGrantedWebViewInput() = runTest {
        val input = object : WebViewInput {
            override val permissionTitleResId = R.string.converter_google_maps_permission_title
            override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title
            override val unsafeExtractionJavascript = "undefined"

            override suspend fun parse(
                data: String,
                match: String,
            ) = throw NotImplementedError()
        }
        val state = PermissionGranted(stateContext, source, match = source, input, permission, listOf(prevResult))
        assertEquals(
            PermissionGrantedWebViewInput(
                stateContext, source, match = source, input, permission, listOf(prevResult)
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputIsNoopInput_returnsDataParsed() = runTest {
        val input = object : NoopInput {}
        val state = PermissionGranted(stateContext, source, match = source, input, permission, listOf(prevResult))
        assertEquals(
            DataParsed(stateContext, source, match = source, input, permission, listOf(ParseResult(), prevResult)),
            state.transition(),
        )
    }
}
