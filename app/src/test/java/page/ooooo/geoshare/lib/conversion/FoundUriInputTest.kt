package page.ooooo.geoshare.lib.conversion

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput

class FoundUriInputTest {
    @Test
    fun transition_whenPermissionIsAlways_returnsGrantedPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, Permission.ALWAYS)
        assertEquals(
            GrantedPermission(stateContext, source, match = source, input),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsAsk_returnsRequestedPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, Permission.ASK)
        assertEquals(
            RequestedPermission(
                stateContext,
                source,
                match = source,
                input,
                input.permissionTitleResId,
                input.loadingIndicatorTitleResId,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, Permission.NEVER)
        assertEquals(
            DeniedPermission(stateContext, source, input),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAlways_returnsGrantedPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ALWAYS
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, permission = null)
        assertEquals(
            GrantedPermission(stateContext, source, match = source, input),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAsk_returnsRequestedPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ASK
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, permission = null)
        assertEquals(
            RequestedPermission(
                stateContext,
                source,
                match = source,
                input,
                input.permissionTitleResId,
                input.loadingIndicatorTitleResId,
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsNever_returnsDeniedUnshortenPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.NEVER
        }
        val stateContext: ConversionStateContext = mock {
            on { userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, permission = null)
        assertEquals(
            DeniedPermission(stateContext, source, input),
            state.transition(),
        )
    }
}
