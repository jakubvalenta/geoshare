package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

sealed interface ConversionStateLogItem {
    val id: Int
    val state: ConversionState.HasDescription

    data class Finished(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        val elapsedTime: Duration,
        val succeeded: Boolean,
    ) : ConversionStateLogItem

    data class Pending(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        val timeMark: ComparableTimeMark,
    ) : ConversionStateLogItem {
        fun finish(succeeded: Boolean, timeSource: TimeSource.Monotonic): Finished =
            Finished(
                id = id,
                state = state,
                elapsedTime = timeSource.markNow() - timeMark,
                succeeded = succeeded,
            )
    }
}
