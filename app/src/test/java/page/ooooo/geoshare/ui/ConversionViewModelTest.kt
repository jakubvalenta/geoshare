package page.ooooo.geoshare.ui

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.FakeBilling
import page.ooooo.geoshare.data.di.FakeConversionStateContext
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.di.FakeLinkRepository
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.FakeLog
import page.ooooo.geoshare.lib.FakeUriQuote
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.ExtendedConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.conversion.SourceReceived
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.testing.MainDispatcherRule
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionViewModelTest {
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_no_points) } doReturn "No points found"
        on { getString(R.string.conversion_processing, "Debug Input") } doReturn "Processing Debug Input..."
    }
    private val context: Context = mock {
        on { resources } doReturn resources
    }
    private val billing = FakeBilling(context)
    private val coordinateConverter: CoordinateConverter = mock()
    private val inputs = listOf(
        FakeInputRepository.debugUriInput,
    )
    private val linkRepository = FakeLinkRepository()
    private val log = FakeLog
    private val outputRepository = OutputRepository(coordinateConverter)
    private val savedStateHandle = SavedStateHandle()
    private val source = "https://maps.google.com/foo"
    private val timeSource = TestTimeSource()
    private val uriQuote = FakeUriQuote
    private val userPreferencesRepository = FakeUserPreferencesRepository()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun extendedStateLog_whenNextItemHasError_returnsFinishedItemWithSucceededFalse() = runTest {
        val stateContext = FakeConversionStateContext(
            inputs = inputs,
            linkRepository = linkRepository,
            outputRepository = outputRepository,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
        val conversionViewModel = ConversionViewModel(
            stateContext = stateContext,
            savedStateHandle = savedStateHandle,
        )
        backgroundScope.launch {
            // Start collecting the StateFlow, so that we can read its value later
            conversionViewModel.extendedStateLog.collect {}
        }

        // Assert initial extended state log value
        advanceUntilIdle()
        assertTrue(conversionViewModel.extendedStateLog.value.isEmpty())

        // Set new state
        val firstState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val firstStart = timeSource.markNow()
        val secondState = ConversionFailed(
            source = source,
            message = resources.getString(R.string.conversion_failed_reason_no_points),
        )
        val secondStart = timeSource.apply { plusAssign(100.milliseconds) }.markNow()
        stateContext.setState(firstState, firstStart)
        stateContext.setState(secondState, secondStart)

        // Assert new extended state log value
        advanceUntilIdle()
        assertEquals(
            listOf(
                ExtendedConversionStateLogItem.Finished(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                    end = secondStart,
                    succeeded = false,
                ),
            ),
            conversionViewModel.extendedStateLog.value,
        )
    }

    @Test
    fun extendedStateLog_whenNextItemHasLastAttempt_returnsFinishedItemWithSucceededFalse() = runTest {
        val stateContext = FakeConversionStateContext(
            inputs = inputs,
            linkRepository = linkRepository,
            outputRepository = outputRepository,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
        val conversionViewModel = ConversionViewModel(
            stateContext = stateContext,
            savedStateHandle = savedStateHandle,
        )
        backgroundScope.launch {
            // Start collecting the StateFlow, so that we can read its value later
            conversionViewModel.extendedStateLog.collect {}
        }

        // Assert initial extended state log value
        advanceUntilIdle()
        assertTrue(conversionViewModel.extendedStateLog.value.isEmpty())

        // Set new state
        val firstState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val firstStart = timeSource.markNow()
        val secondState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
            lastAttempt = Attempt(1, ConnectTimeoutNetworkException(Exception())),
        )
        val secondStart = timeSource.apply { plusAssign(100.milliseconds) }.markNow()
        stateContext.setState(firstState, firstStart)
        stateContext.setState(secondState, secondStart)

        // Assert new extended state log value
        advanceUntilIdle()
        assertEquals(
            listOf(
                ExtendedConversionStateLogItem.Finished(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                    end = secondStart,
                    succeeded = false,
                ),
                ExtendedConversionStateLogItem.Pending(
                    id = 1,
                    state = secondState,
                    start = secondStart,
                ),
            ),
            conversionViewModel.extendedStateLog.value,
        )
    }

    @Test
    fun extendedStateLog_whenNextItemDoesNotHaveErrorOrLastAttempt_returnsFinishedItemWithSucceededTrue() = runTest {
        val stateContext = FakeConversionStateContext(
            inputs = inputs,
            linkRepository = linkRepository,
            outputRepository = outputRepository,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
        val conversionViewModel = ConversionViewModel(
            stateContext = stateContext,
            savedStateHandle = savedStateHandle,
        )
        backgroundScope.launch {
            // Start collecting the StateFlow, so that we can read its value later
            conversionViewModel.extendedStateLog.collect {}
        }

        // Assert initial extended state log value
        advanceUntilIdle()
        assertTrue(conversionViewModel.extendedStateLog.value.isEmpty())

        // Set new state
        val firstState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val firstStart = timeSource.markNow()
        val secondState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val secondStart = timeSource.apply { plusAssign(100.milliseconds) }.markNow()
        stateContext.setState(firstState, firstStart)
        stateContext.setState(secondState, secondStart)

        // Assert new extended state log value
        advanceUntilIdle()
        assertEquals(
            listOf(
                ExtendedConversionStateLogItem.Finished(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                    end = secondStart,
                    succeeded = true,
                ),
                ExtendedConversionStateLogItem.Pending(
                    id = 1,
                    state = secondState,
                    start = secondStart,
                ),
            ),
            conversionViewModel.extendedStateLog.value,
        )
    }

    @Test
    fun extendedStateLog_whenThereIsNoNextItem_returnsPendingItem() = runTest {
        val stateContext = FakeConversionStateContext(
            inputs = inputs,
            linkRepository = linkRepository,
            outputRepository = outputRepository,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
        val conversionViewModel = ConversionViewModel(
            stateContext = stateContext,
            savedStateHandle = savedStateHandle,
        )
        backgroundScope.launch {
            // Start collecting the StateFlow, so that we can read its value later
            conversionViewModel.extendedStateLog.collect {}
        }

        // Assert initial extended state log value
        advanceUntilIdle()
        assertTrue(conversionViewModel.extendedStateLog.value.isEmpty())

        // Set new state
        val firstState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val firstStart = timeSource.markNow()
        stateContext.setState(firstState, firstStart)

        // Assert new extended state log value
        advanceUntilIdle()
        assertEquals(
            listOf(
                ExtendedConversionStateLogItem.Pending(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                ),
            ),
            conversionViewModel.extendedStateLog.value,
        )
    }

    @Test
    fun extendedStateLog_whenThereAreItemsWithoutDescription_returnsOnlyItemsWithDescription() = runTest {
        val stateContext = FakeConversionStateContext(
            inputs = inputs,
            linkRepository = linkRepository,
            outputRepository = outputRepository,
            resources = resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
        val conversionViewModel = ConversionViewModel(
            stateContext = stateContext,
            savedStateHandle = savedStateHandle,
        )
        backgroundScope.launch {
            // Start collecting the StateFlow, so that we can read its value later
            conversionViewModel.extendedStateLog.collect {}
        }

        // Assert initial extended state log value
        advanceUntilIdle()
        assertTrue(conversionViewModel.extendedStateLog.value.isEmpty())

        // Set new state
        val firstState = SourceReceived(
            source = source,
        )
        val firstStart = timeSource.markNow()
        val secondState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val secondStart = timeSource.markNow()
        val thirdState = ConversionSucceeded(
            source = source,
            points = persistentListOf(),
        )
        val thirdStart = timeSource.markNow()
        stateContext.setState(firstState, firstStart)
        stateContext.setState(secondState, secondStart)
        stateContext.setState(thirdState, thirdStart)

        // Assert new extended state log value
        advanceUntilIdle()
        assertEquals(
            listOf(
                ExtendedConversionStateLogItem.Finished(
                    id = 1,
                    state = secondState,
                    start = secondStart,
                    end = thirdStart,
                    succeeded = true,
                ),
            ),
            conversionViewModel.extendedStateLog.value,
        )
    }
}
