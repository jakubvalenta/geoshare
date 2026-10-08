package page.ooooo.geoshare.lib.state

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.BasicOfflineInput
import page.ooooo.geoshare.lib.inputs.BasicOnlineInput
import page.ooooo.geoshare.lib.inputs.InputGroup
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.inputs.NoopInput
import page.ooooo.geoshare.lib.inputs.ParseResult
import page.ooooo.geoshare.lib.inputs.WebViewInput
import page.ooooo.geoshare.lib.network.FetchTools

class InputMatchedTest {
    private val log = FakeLog
    private val source = "https://maps.app.goo.gl/foo"
    private val input = object : BasicOnlineInput {
        override fun getName(resources: Resources) = "Test Input"
        override val group = InputGroup.DEBUG

        override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools): ParseResult {
            throw NotImplementedError()
        }
    }
    private val matchedInput = MatchedInput(input, source)
    private val oldPoints = persistentListOf(WGS84Point(1.0, 2.0, source = Source.GENERATED))
    private val oldResult = ParseResult.Success(oldPoints)
    private val resources: Resources = mock()
    private val results: Results = mapOf(MatchedInput(FakeInputRepository.debugUriInput, source) to oldResult)

    @Test
    fun transition_whenPermissionIsAlways_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.ALWAYS, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.ALWAYS, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenPermissionIsAsk_returnsPermissionRequested() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.ASK, results)
        assertEquals(
            PermissionRequested(source, matchedInput, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenPermissionIsNever_returnsPermissionDenied() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAlways_returnsPermissionGrantedAndSetsPermissionParam() =
        runTest {
            val userPreferencesRepository = FakeUserPreferencesRepository(
                UserPreferencesValues(connectionPermission = Permission.ALWAYS)
            )
            val stateContext = ConversionStateContext(
                log = log,
                resources = resources,
                userPreferencesRepository = userPreferencesRepository,
            )
            val state = InputMatched(source, matchedInput, permission = null, results)
            assertEquals(
                PermissionGranted(source, matchedInput, Permission.ALWAYS, results),
                state.transition(stateContext),
            )
        }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsAsk_returnsPermissionRequested() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, permission = null, results)
        assertEquals(
            PermissionRequested(source, matchedInput, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenPermissionIsNullAndPreferencePermissionIsNever_returnsPermissionDenied() = runTest {
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.NEVER)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, permission = null, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputIsNoopInput_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val input = object : NoopInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG
        }
        val matchedInput = MatchedInput(input, source)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.NEVER, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputIsBasicOfflineInput_returnsPermissionGrantedAndPassesPermissionParam() = runTest {
        val input = object : BasicOfflineInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            override fun parse(match: String, resources: Resources): ParseResult {
                throw NotImplementedError()
            }
        }
        val matchedInput = MatchedInput(input, source)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionGranted(source, matchedInput, Permission.NEVER, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputIsWebViewInputAndPermissionIsNever_returnsPermissionDenied() = runTest {
        val input = object : WebViewInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            @Suppress("SameReturnValue")
            override fun getUnsafeExtractionJavaScript() = "undefined"

            override fun parse(data: String, match: String, resources: Resources): ParseResult {
                throw NotImplementedError()
            }
        }
        val matchedInput = MatchedInput(input, source)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.transition(stateContext),
        )
    }

    @Test
    fun transition_whenInputIsNoopInputAndWebViewInputAndPermissionIsNever_returnsPermissionDenied() = runTest {
        val input = object : NoopInput, WebViewInput {
            override fun getName(resources: Resources) = "Test Input"
            override val group = InputGroup.DEBUG

            @Suppress("SameReturnValue")
            override fun getUnsafeExtractionJavaScript() = "undefined"

            override fun parse(data: String, match: String, resources: Resources): ParseResult {
                throw NotImplementedError()
            }
        }
        val matchedInput = MatchedInput(input, source)
        val userPreferencesRepository = FakeUserPreferencesRepository(
            UserPreferencesValues(connectionPermission = Permission.ASK)
        )
        val stateContext = ConversionStateContext(
            log = log,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
        )
        val state = InputMatched(source, matchedInput, Permission.NEVER, results)
        assertEquals(
            PermissionDenied(source, matchedInput, results),
            state.transition(stateContext),
        )
    }
}
