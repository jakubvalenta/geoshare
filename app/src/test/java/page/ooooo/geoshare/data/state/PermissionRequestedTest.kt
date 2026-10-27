package page.ooooo.geoshare.data.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeUserPreferenceRepository
import page.ooooo.geoshare.data.geo.Source
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.inputs.MatchedInput
import page.ooooo.geoshare.data.inputs.ParseResult
import page.ooooo.geoshare.data.preferences.ConnectionPermissionPreference
import page.ooooo.geoshare.data.preferences.Permission
import page.ooooo.geoshare.data.preferences.UserPreferenceValues

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
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferenceRepository } doReturn userPreferenceRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.ALWAYS, results),
            state.grant(stateContext, false),
        )
        assertEquals(
            Permission.ASK,
            userPreferenceRepository.getValue(ConnectionPermissionPreference),
        )
    }

    @Test
    fun grant_whenDoNotAskIsTrue_savesPreferenceAndReturnsPermissionGranted() = runTest {
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferenceRepository } doReturn userPreferenceRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.ALWAYS, results),
            state.grant(stateContext, true),
        )
        assertEquals(
            Permission.ALWAYS,
            userPreferenceRepository.getValue(ConnectionPermissionPreference),
        )
    }

    @Test
    fun deny_whenDoNotAskIsFalse_doesNotSavePreferenceAndReturnsPermissionDenied() = runTest {
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferenceRepository } doReturn userPreferenceRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.deny(stateContext, false),
        )
        assertEquals(
            Permission.ASK,
            userPreferenceRepository.getValue(ConnectionPermissionPreference),
        )
    }

    @Test
    fun deny_whenDoNotIsAskIsTrue_savesPreferenceAndReturnsPermissionDenied() = runTest {
        val userPreferenceRepository = FakeUserPreferenceRepository(
            UserPreferenceValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.userPreferenceRepository } doReturn userPreferenceRepository
        }
        val state = PermissionRequested(source, matchedInput, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.deny(stateContext, true),
        )
        assertEquals(
            Permission.NEVER,
            userPreferenceRepository.getValue(ConnectionPermissionPreference),
        )
    }
}
