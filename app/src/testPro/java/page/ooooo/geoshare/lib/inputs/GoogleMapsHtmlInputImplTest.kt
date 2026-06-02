package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R

class GoogleMapsHtmlInputImplTest : InputTest {
    private val input = GoogleMapsHtmlInputImpl()
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_unsupported_source) } doReturn "This link is not supported"
    }

    @Test
    fun getErrorMessage_returnsCustomMessage() = runTest {
        assertEquals("This link is not supported", input.getErrorMessage(resources))
    }
}
