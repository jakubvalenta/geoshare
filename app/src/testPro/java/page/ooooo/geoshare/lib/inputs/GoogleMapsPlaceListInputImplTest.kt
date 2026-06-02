package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R

class GoogleMapsPlaceListInputImplTest : InputTest {
    private val input = GoogleMapsPlaceListInputImpl()
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_unsupported_source_place_list) } doReturn "Place lists are not supported"
    }

    @Test
    fun getErrorMessage_returnsCustomMessage() = runTest {
        assertEquals("Place lists are not supported", input.getErrorMessage(resources))
    }
}
