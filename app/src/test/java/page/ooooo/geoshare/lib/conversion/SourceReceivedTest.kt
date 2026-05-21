package page.ooooo.geoshare.lib.conversion

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository

class SourceReceivedTest {
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_missing_url) } doReturn "Missing URL"
        on { getString(R.string.conversion_failed_unsupported_service) } doReturn "Unsupported map service"
    }
    private val stateContext: ConversionStateContext = mock {
        on { inputs } doReturn listOf(FakeInputRepository.geoUriInput, FakeInputRepository.osmAndUriInput)
        on { this@on.resources } doReturn resources
    }

    @Test
    fun transition_whenSourceIsEmpty_returnsConversionFailed() = runTest {
        val source = ""
        val state = SourceReceived(stateContext, "")
        assertEquals(
            ConversionFailed(source, resources.getString(R.string.conversion_failed_missing_url)),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceIsGeoUri_returnsInputFound() = runTest {
        val source = "geo:1,2?q="
        val state = SourceReceived(stateContext, source)
        assertEquals(
            InputFound(stateContext, source, match = source, FakeInputRepository.geoUriInput, permission = null),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceHasUriInTheMiddle_returnsInputFound() = runTest {
        val source = "FOO\nhttps://www.osmand.net/foo\nBAR"
        val match = "https://www.osmand.net/foo"
        val state = SourceReceived(stateContext, source)
        assertEquals(
            InputFound(stateContext, source, match, FakeInputRepository.osmAndUriInput, permission = null),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceMatchesAnInput_returnsInputFound() = runTest {
        val source = "https://www.osmand.net/foo"
        val state = SourceReceived(stateContext, source)
        assertEquals(
            InputFound(stateContext, source, match = source, FakeInputRepository.osmAndUriInput, permission = null),
            state.transition(),
        )
    }

    @Test
    fun transition_whenSourceDoesNotMatchAnyInput_returnsConversionFailed() = runTest {
        val source = "https://maps.example.com/foo"
        val state = SourceReceived(stateContext, source)
        assertEquals(
            ConversionFailed(
                source,
                resources.getString(R.string.conversion_failed_unsupported_service)
            ),
            state.transition(),
        )
    }
}
