package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class PermissionDeniedTest {
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_connection_permission_denied) } doReturn "This link is not supported without connecting to the map service"
    }
    private val stateContext: ConversionStateContext = mock {
        on { this@on.resources } doReturn resources
    }

    @Test
    fun transition_whenPrevResultIsNotNull_returnsConversionFailed() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val state = PermissionDenied(stateContext, source, match = source, GoogleMapsUriInput, Permission.NEVER)
        assertEquals(
            ConversionFailed(
                resources.getString(R.string.conversion_failed_connection_permission_denied),
                source
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPrevResultIsNull_returnsDataParsed() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val state = PermissionDenied(
            stateContext, source, match = source, GoogleMapsUriInput, Permission.NEVER, prevResult
        )
        assertEquals(
            DataParsed(
                stateContext, source, match = source, GoogleMapsUriInput, prevResult, Permission.NEVER
            ),
            state.transition(),
        )
    }
}
