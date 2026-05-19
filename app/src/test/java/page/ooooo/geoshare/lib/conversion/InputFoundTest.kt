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
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.ParseResult

class InputFoundTest {
    private val log = FakeLog
    private val source = "https://maps.app.goo.gl/foo"
    private val input = GoogleMapsHtmlInput(
        googleMapsUriInput = { throw NotImplementedError() },
        googleMapsWebViewInput = { throw NotImplementedError() },
    )
    private val prevPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val prevResult = ParseResult(prevPoints)

    @Test
    fun transition_whenPermissionIsAlways_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputFound(stateContext, source, match = source, input, Permission.ALWAYS, prevResult)
        assertEquals(
            PermissionGranted(stateContext, source, match = source, input, Permission.ALWAYS, prevResult),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsAsk_returnsPermissionRequested() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputFound(stateContext, source, match = source, input, Permission.ASK, prevResult)
        assertEquals(
            PermissionRequested(stateContext, source, match = source, input, prevResult, input.permissionTitleResId),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNever_returnsDataParsed() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doThrow NotImplementedError()
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputFound(stateContext, source, match = source, input, Permission.NEVER, prevResult)
        assertEquals(
            DataParsed(
                stateContext, source, match = source, input, result = ParseResult(), Permission.NEVER, prevResult
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAlways_returnsPermissionGrantedAndSetsPermissionParam() =
        runTest {
            val userPreferencesRepository: FakeUserPreferencesRepository = mock {
                on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ALWAYS
            }
            val stateContext: ConversionStateContext = mock {
                on { this@on.log } doReturn log
                on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
            }
            val state = InputFound(stateContext, source, match = source, input, permission = null, prevResult)
            assertEquals(
                PermissionGranted(stateContext, source, match = source, input, Permission.ALWAYS, prevResult),
                state.transition(),
            )
        }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAsk_returnsPermissionRequested() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.ASK
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputFound(stateContext, source, match = source, input, permission = null, prevResult)
        assertEquals(
            PermissionRequested(stateContext, source, match = source, input, prevResult, input.permissionTitleResId),
            state.transition(),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsNever_returnsDataParsed() = runTest {
        val userPreferencesRepository: FakeUserPreferencesRepository = mock {
            on { getValue(eq(ConnectionPermissionPreference)) } doReturn Permission.NEVER
        }
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
            on { this@on.userPreferencesRepository } doReturn userPreferencesRepository
        }
        val state = InputFound(stateContext, source, match = source, input, permission = null, prevResult)
        assertEquals(
            DataParsed(
                stateContext, source, match = source, input, result = ParseResult(), Permission.NEVER, prevResult
            ),
            state.transition(),
        )
    }

    @Test
    fun transition_whenInputDoesNotHavePermission_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val input = GeoUriInput()
        val stateContext: ConversionStateContext = mock {
            on { this@on.log } doReturn log
        }
        val state = InputFound(stateContext, source, match = source, input, Permission.NEVER, prevResult)
        assertEquals(
            PermissionGranted(stateContext, source, match = source, input, Permission.NEVER, prevResult),
            state.transition(),
        )
    }
}
