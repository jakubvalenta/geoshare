package page.ooooo.geoshare.ui

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.conversion.ConversionFailed
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionSucceeded
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.conversion.SourceReceived
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.lib.state.ExtendedStateLogItem
import page.ooooo.geoshare.lib.state.StateLog
import page.ooooo.geoshare.lib.state.StateLogItem
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

class ConversionViewModelTest {
    private val resources: Resources = mock {
        on { getString(R.string.conversion_failed_cancelled) } doReturn "Cancelled"
        on { getString(R.string.conversion_failed_reason_no_points) } doReturn "No points found"
        on { getString(R.string.conversion_processing, "Debug Input") } doReturn "Processing Debug Input..."
    }
    private val source = "https://maps.google.com/foo"
    private val timeSource = TestTimeSource()

    @Test
    fun toExtendedLog_whenNextItemHasError_returnsFinishedItemWithSucceededFalse() {
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
        val stateLog: StateLog<ConversionState> = listOf(
            StateLogItem(0, firstState, firstStart),
            StateLogItem(1, secondState, secondStart),
        )
        assertEquals(
            listOf(
                ExtendedStateLogItem.Finished<ConversionState.HasDescription>(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                    end = secondStart,
                    succeeded = false,
                ),
            ),
            stateLog.toExtendedLog(),
        )
    }

    @Test
    fun toExtendedLog_whenNextItemHasLastAttempt_returnsFinishedItemWithSucceededFalse() {
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
        val stateLog: StateLog<ConversionState> = listOf(
            StateLogItem(0, firstState, firstStart),
            StateLogItem(1, secondState, secondStart),
        )
        assertEquals(
            listOf(
                ExtendedStateLogItem.Finished<ConversionState.HasDescription>(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                    end = secondStart,
                    succeeded = false,
                ),
                ExtendedStateLogItem.Pending<ConversionState.HasDescription>(
                    id = 1,
                    state = secondState,
                    start = secondStart,
                ),
            ),
            stateLog.toExtendedLog(),
        )
    }

    @Test
    fun toExtendedLog_whenNextItemDoesNotHaveErrorOrLastAttempt_returnsFinishedItemWithSucceededTrue() {
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
        val stateLog: StateLog<ConversionState> = listOf(
            StateLogItem(0, firstState, firstStart),
            StateLogItem(1, secondState, secondStart),
        )
        assertEquals(
            listOf(
                ExtendedStateLogItem.Finished<ConversionState.HasDescription>(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                    end = secondStart,
                    succeeded = true,
                ),
                ExtendedStateLogItem.Pending<ConversionState.HasDescription>(
                    id = 1,
                    state = secondState,
                    start = secondStart,
                ),
            ),
            stateLog.toExtendedLog(),
        )
    }

    @Test
    fun toExtendedLog_whenThereIsNoNextItem_returnsPendingItem() {
        val firstState = PermissionGrantedBasicInput(
            source = source,
            matchedInput = MatchedInput(FakeInputRepository.debugUriInput, source),
            permission = null,
            results = emptyMap(),
        )
        val firstStart = timeSource.markNow()
        val stateLog: StateLog<ConversionState> = listOf(
            StateLogItem(0, firstState, firstStart),
        )
        assertEquals(
            listOf(
                ExtendedStateLogItem.Pending<ConversionState.HasDescription>(
                    id = 0,
                    state = firstState,
                    start = firstStart,
                ),
            ),
            stateLog.toExtendedLog(),
        )
    }

    @Test
    fun toExtendedLog_whenThereAreItemsWithoutDescription_returnsOnlyItemsWithDescription() {
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
        val stateLog: StateLog<ConversionState> = listOf(
            StateLogItem(0, firstState, firstStart),
            StateLogItem(1, secondState, secondStart),
            StateLogItem(2, thirdState, thirdStart),
        )
        assertEquals(
            listOf(
                ExtendedStateLogItem.Finished<ConversionState.HasDescription>(
                    id = 1,
                    state = secondState,
                    start = secondStart,
                    end = thirdStart,
                    succeeded = true,
                ),
            ),
            stateLog.toExtendedLog(),
        )
    }
}
