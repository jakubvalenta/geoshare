package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
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
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class PermissionRequestedTest {
    @Test
    fun transition_returnsNull() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val stateContext: ConversionStateContext = mock()
        val state = PermissionRequested(
            stateContext, source, match = source, input, prevResult, input.permissionTitleResId
        )
        assertNull(state.transition())
    }

    @Test
    fun grant_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsPermissionGranted() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(
            stateContext, source, match = source, input, prevResult, input.permissionTitleResId
        )
        assertEquals(
            PermissionGranted(stateContext, source, match = source, input, Permission.ALWAYS, prevResult),
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
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state =
            PermissionRequested(stateContext, source, match = source, input, prevResult, input.permissionTitleResId)
        assertEquals(
            PermissionGranted(stateContext, source, match = source, input, Permission.ALWAYS, prevResult),
            state.grant(true),
        )
        verify(userPreferencesRepository).setValue(
            ConnectionPermissionPreference,
            Permission.ALWAYS,
        )
    }

    @Test
    fun deny_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsDataParsed() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state =
            PermissionRequested(stateContext, source, match = source, input, prevResult, input.permissionTitleResId)
        assertEquals(
            DataParsed(
                stateContext, source, match = source, input, result = ParseResult(), Permission.NEVER, prevResult
            ),
            state.deny(false),
        )
        verify(userPreferencesRepository, never()).setValue(
            eq(ConnectionPermissionPreference),
            any<Permission>(),
        )
    }

    @Test
    fun deny_whenDoNotIsAskIsTrue_savesPreferenceAndReturnsDataParsed() = runTest {
        val source = "https://maps.app.goo.gl/foo"
        val input = GoogleMapsHtmlInput
        val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
        val prevResult = ParseResult(prevPoints)
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { setValue(eq(ConnectionPermissionPreference), any()) } doReturn Unit
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(
            stateContext, source, match = source, input, prevResult, input.permissionTitleResId
        )
        assertEquals(
            DataParsed(
                stateContext, source, match = source, input, result = ParseResult(), Permission.NEVER, prevResult
            ),
            state.deny(true),
        )
        verify(userPreferencesRepository).setValue(
            ConnectionPermissionPreference,
            Permission.NEVER,
        )
    }
}
