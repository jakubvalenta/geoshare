package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ReceivedSourceDataTest {
    @Test
    fun receivedUriString_inputUriStringIsEmpty_returnsConversionFailed() = runTest {
        val inputUriString = ""
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, "")
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_missing_url), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsGeoUri_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "geo:1,2?q="
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            FoundInput(stateContext, inputUriString, GeoUriInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringHasUriInTheMiddle_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "FOO\nhttps://maps.google.com/foo\nBAR"
        val matchedInputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(matchedInputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsValidUrl_returnsReceivedUriWithPermissionNull() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, null),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringIsNotValidUrl_returnsConversionFailed() = runTest {
        val inputUriString = "https://[invalid:ipv6]/"
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotHaveScheme_returnsConversionFailed() = runTest {
        val inputUriString = "maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringHasRelativeScheme_returnsConversionFailed() = runTest {
        val inputUriString = "//maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotHaveHttpsScheme_returnsConversionFailed() = runTest {
        val inputUriString = "ftp://maps.google.com/"
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

    @Test
    fun receivedUriString_inputUriStringDoesNotMatchAnyInput_returnsConversionFailed() = runTest {
        val inputUriString = "https://maps.example.com/foo"
        val stateContext = mockStateContext()
        val state = ReceivedSourceData(stateContext, inputUriString)
        assertEquals(
            ConversionFailed(mockResources.getString(R.string.conversion_failed_unsupported_service), inputUriString),
            state.transition(),
        )
    }

}
