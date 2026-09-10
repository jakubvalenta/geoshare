package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R

class GoogleMapsPlaceListInputImplTest : InputTest {
    override val resources: Resources = mock {
        on { getString(R.string.conversion_failed_unsupported_source_place_list) } doReturn "Place lists are not supported"
    }
    private val input = GoogleMapsPlaceListInputImpl()

    @Test
    fun parse_returnsWarning() = runTest {
        assertEquals(
            ParseResult.Warning(resources.getString(R.string.conversion_failed_unsupported_source_place_list)),
            input.fetchAndParse("https://www.google.com/maps/placelists/list/mfmnkPs6RuGyp0HOmXLSKg"),
        )
    }
}
