package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput

class DeniedPermissionTest {
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_connection_permission_denied) } doReturn "This link is not supported without connecting to the map service"
    }
    private val stateContext: ConversionStateContext = mock {
        on { resources } doReturn resources
    }

    @Test
    fun transition_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val state = DeniedPermission(stateContext, inputUriString, GoogleMapsUriInput)
        assertEquals(
            ConversionFailed(
                resources.getString(R.string.conversion_failed_connection_permission_denied),
                inputUriString
            ),
            state.transition(),
        )
    }
}
