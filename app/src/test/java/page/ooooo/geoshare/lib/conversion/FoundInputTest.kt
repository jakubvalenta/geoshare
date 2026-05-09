package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
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
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput

class FoundInputTest {
    @Test
    fun transition_whenPermissionIsAlways_returnsGrantedPermission() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, Permission.ALWAYS, prevPoints)
        assertEquals(
            GrantedPermission(
                stateContext,
                source,
                match = source,
                input,
                input.loadingIndicatorTitleResId,
                Permission.ALWAYS,
                prevPoints,
            ),
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
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
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
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
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
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ALWAYS
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, permission = null, prevPoints)
        assertEquals(
            GrantedPermission(
                stateContext,
                source,
                match = source,
                input,
                input.loadingIndicatorTitleResId,
                Permission.ALWAYS,
                prevPoints,
            ),
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
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
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
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = FoundInput(stateContext, source, match = source, input, permission = null)
        assertEquals(
            DeniedPermission(stateContext, source, input),
            state.transition(),
        )
    }
}
