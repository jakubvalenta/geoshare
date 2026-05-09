package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput

class ReceivedSourceDataTest {
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_missing_url) } doReturn "Missing URL"
        on { getString(R.string.conversion_failed_unsupported_service) } doReturn "Unsupported map service"
    }
    private val stateContext: ConversionStateContext = mock {
        on { inputs } doReturn listOf(GeoUriInput, GoogleMapsUriInput)
        on { this@on.resources } doReturn resources
    }

    @Test
    fun transition_whenSourceIsEmpty_returnsConversionFailed() = runTest {
        val source = ""
        val state = ReceivedSourceData(stateContext, "")
        assertEquals(
            ConversionFailed(resources.getString(R.string.conversion_failed_missing_url), source),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceIsGeoUri_returnsFoundInput() = runTest {
        val source = "geo:1,2?q="
        val state = ReceivedSourceData(stateContext, source)
        assertEquals(
            FoundInput(stateContext, source, match = source, GeoUriInput, permission = null),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceHasUriInTheMiddle_returnsFoundInput() = runTest {
        val source = "FOO\nhttps://maps.google.com/foo\nBAR"
        val match = "https://maps.google.com/foo"
        val state = ReceivedSourceData(stateContext, source)
        assertEquals(
            FoundInput(stateContext, source, match, GoogleMapsUriInput, permission = null),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceMatchesAnInput_returnsFoundInput() = runTest {
        val source = "https://maps.google.com/foo"
        val state = ReceivedSourceData(stateContext, source)
        assertEquals(
            FoundInput(stateContext, source, match = source, GoogleMapsUriInput, permission = null),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceDoesNotMatchAnyInput_returnsConversionFailed() = runTest {
        val source = "https://maps.example.com/foo"
        val state = ReceivedSourceData(stateContext, source)
        assertEquals(
            ConversionFailed(resources.getString(R.string.conversion_failed_unsupported_service), source),
            state.transition(),
        )
    }
}
