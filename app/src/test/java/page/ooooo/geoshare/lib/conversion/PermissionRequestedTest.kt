package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput

class PermissionRequestedTest {
    @Test
    fun transition_returnsNull() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val stateContext: ConversionStateContext = mock()
        val state = PermissionRequested(
            stateContext, source, match = source, input, input.permissionTitleResId
        )
        assertNull(state.transition())
    }

    @Test
    fun grant_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsPermissionGranted() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(
            stateContext, source, match = source, input, input.permissionTitleResId
        )
        assertEquals(
            PermissionGranted(stateContext, source, match = source, input, Permission.ALWAYS),
            state.grant(false),
        )
        verify(userPreferencesRepository, never()).setValue(
            eq(ConnectionPermissionPreference),
            any<Permission>(),
        )
    }

    @Test
    fun grant_whenDoNotAskIsTrue_savesPreferenceAndReturnsPermissionGranted() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(stateContext, source, match = source, input, input.permissionTitleResId)
        assertEquals(
            PermissionGranted(stateContext, source, match = source, input, Permission.ALWAYS),
            state.grant(true),
        )
        verify(userPreferencesRepository).setValue(
            ConnectionPermissionPreference,
            Permission.ALWAYS,
        )
    }

    @Test
    fun deny_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsPermissionDenied() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(stateContext, source, match = source, input, input.permissionTitleResId)
        assertEquals(
            PermissionDenied(stateContext, source, input),
            state.deny(false),
        )
        verify(userPreferencesRepository, never()).setValue(
            eq(ConnectionPermissionPreference),
            any<Permission>(),
        )
    }

    @Test
    fun deny_whenDoNotIsAskIsTrue_savesPreferenceAndPermissionDenied() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(stateContext, source, match = source, input, input.permissionTitleResId)
        assertEquals(
            PermissionDenied(stateContext, source, input),
            state.deny(true),
        )
        verify(userPreferencesRepository).setValue(
            ConnectionPermissionPreference,
            Permission.NEVER,
        )
    }
}
