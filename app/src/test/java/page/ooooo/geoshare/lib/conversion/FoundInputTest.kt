package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class FoundInputTest {
    @Test
    fun receivedUri_inputSupportsShortUriAndItDoesNotMatchTheUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.google.com/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val stateContext = mockStateContext()
        val state = FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, GoogleMapsUriInput, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputDoesNotSupportShortUri_returnsUnshortenedUrlAndPassesPermission() = runTest {
        val inputUriString = "https://maps.app.goo.gl/foo"
        val uri = Uri.parse(inputUriString, uriQuote)
        val mockInput = object : Input {
            override val pattern = Regex(".")
            override val documentation =
                InputDocumentation(id = GeoUriInput.documentation.id, nameResId = -1, items = emptyList())

            override suspend fun parseUri(uri: Uri, uriQuote: UriQuote) = ParseUriResult(persistentListOf())
        }
        val stateContext = mockStateContext(inputs = listOf(mockInput))
        val state = FoundInput(stateContext, inputUriString, mockInput, uri, Permission.NEVER)
        assertEquals(
            UnshortenedUrl(stateContext, inputUriString, mockInput, uri, Permission.NEVER),
            state.transition(),
        )
    }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = FoundInput(
                stateContext, inputUriString, GoogleMapsUriInput, uri, Permission.ALWAYS
            )
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, Permission.ASK)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, Permission.NEVER)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsUriInput),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ALWAYS
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, null)
            assertEquals(
                GrantedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ASK
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, null)
            assertEquals(
                RequestedUnshortenPermission(stateContext, inputUriString, GoogleMapsUriInput, uri),
                state.transition(),
            )
        }

    @Test
    fun receivedUri_inputSupportsShortUriAndItMatchesTheUriAndPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() =
        runTest {
            val inputUriString = "https://maps.app.goo.gl/foo"
            val uri = Uri.parse(inputUriString, uriQuote)
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.NEVER
            }
            val stateContext = mockStateContext(userPreferencesRepository = mockUserPreferencesRepository)
            val state = FoundInput(stateContext, inputUriString, GoogleMapsUriInput, uri, null)
            assertEquals(
                DeniedConnectionPermission(stateContext, inputUriString, GoogleMapsUriInput),
                state.transition(),
            )
        }
}
