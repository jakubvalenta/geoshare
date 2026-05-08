package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class DeniedPermissionTest {
    @Test
    fun deniedConnectionPermission_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val stateContext = mockStateContext()
        val state = DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsUriInput)
        assertEquals(
            ConversionFailed(
                mockResources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString
            ),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_lastPointHasCoords_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_lastPointHasName_returnsConversionSucceeded() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(name = "bar", source = Source.GENERATED))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionSucceeded(stateContext, inputUriString, points),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_lastPointIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf(WGS84Point(source = Source.GENERATED))
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionFailed(
                mockResources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString,
            ),
            state.transition(),
        )
    }

    @Test
    fun deniedParseHtmlPermission_pointsAreEmpty_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.apple.com/foo"
        val points = persistentListOf<WGS84Point>()
        val stateContext = mockStateContext()
        val state = DeniedParseHtmlPermission(stateContext, inputUriString, points)
        assertEquals(
            ConversionFailed(
                mockResources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString,
            ),
            state.transition(),
        )
    }

}
