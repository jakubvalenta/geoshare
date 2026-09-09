package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class PermissionRequestedTest {
    private val source = "https://maps.app.goo.gl/foo"
    private val input = FakeInputRepository.googleMapsShortLinkInput
    private val matchedInput = MatchedInput(input, source)
    private val oldPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val oldResult = ParseResult.Success(oldPoints)
    private val results: Results = mapOf(MatchedInput(FakeInputRepository.debugUriInput, source) to oldResult)

    @Test
    fun transition_returnsNull() = runTest {
        val stateContext: ConversionStateContext = mock()
        val state = PermissionRequested(source, matchedInput, results)
        assertNull(state.transition(stateContext))
    }

    @Test
    fun grant_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsPermissionGranted() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.ALWAYS, results),
            state.grant(stateContext, false),
        )
        assertEquals(
            userPreferencesRepository.getValue(ConnectionPermissionPreference),
            Permission.ASK,
        )
    }

    @Test
    fun grant_whenDoNotAskIsTrue_savesPreferenceAndReturnsPermissionGranted() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.ALWAYS, results),
            state.grant(stateContext, true),
        )
        assertEquals(
            userPreferencesRepository.getValue(ConnectionPermissionPreference),
            Permission.ALWAYS,
        )
    }

    @Test
    fun deny_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsPermissionDenied() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.deny(stateContext, false),
        )
        assertEquals(
            userPreferencesRepository.getValue(ConnectionPermissionPreference),
            Permission.ASK,
        )
    }

    @Test
    fun deny_whenDoNotIsAskIsTrue_savesPreferenceAndReturnsPermissionDenied() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.deny(stateContext, true),
        )
        assertEquals(
            userPreferencesRepository.getValue(ConnectionPermissionPreference),
            Permission.NEVER,
        )
    }
}
