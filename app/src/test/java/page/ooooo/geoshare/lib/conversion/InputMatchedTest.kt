package page.ooooo.geoshare.lib.conversion

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class InputMatchedTest {
    private val log = FakeLog
    private val source = "https://maps.app.goo.gl/foo"
    private val input = FakeInputRepository.googleMapsShortLinkInput
    private val matchedInput = MatchedInput(input, source)
    private val oldPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val oldResult = ParseResult(oldPoints)
    private val results: Results = mapOf(MatchedInput(FakeInputRepository.debugUriInput, source) to oldResult)

    @Test
    fun transition_whenPermissionIsAlways_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputMatched(stateContext, source, matchedInput, Permission.ALWAYS, results)
        assertEquals(
            PermissionGranted(stateContext, source, matchedInput, Permission.ALWAYS, results),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsAsk_returnsPermissionRequested() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputMatched(stateContext, source, matchedInput, Permission.ASK, results)
        assertEquals(
            PermissionRequested(stateContext, source, matchedInput, results, input.permissionTitleResId),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNever_returnsPermissionDenied() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputMatched(stateContext, source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionDenied(stateContext, source, matchedInput, results),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAlways_returnsPermissionGrantedAndSetsPermissionParam() =
        runTest {
            val userPreferencesRepository = FakeUserPreferencesRepository(
                UserPreferencesValues(connectionPermission = Permission.ALWAYS)
            )
            val stateContext: ConversionStateContext = mock {
                on { this@on.log } doReturn log
                on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
            }
            val state = InputMatched(stateContext, source, matchedInput, permission = null, results)
            assertEquals(
                PermissionGranted(stateContext, source, matchedInput, Permission.ALWAYS, results),
                state.transition(),
            )
        }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAsk_returnsPermissionRequested() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputMatched(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            PermissionRequested(stateContext, source, matchedInput, results, input.permissionTitleResId),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsNever_returnsPermissionDenied() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.NEVER)
        )
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputMatched(stateContext, source, matchedInput, permission = null, results)
        assertEquals(
            PermissionDenied(stateContext, source, matchedInput, results),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputDoesNotHavePermission_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val input = FakeInputRepository.geoUriInput
        val matchedInput = MatchedInput(input, source)
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
        }
        val state = InputMatched(stateContext, source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionGranted(stateContext, source, matchedInput, Permission.NEVER, results),
            state.transition(),
        )
    }
}
