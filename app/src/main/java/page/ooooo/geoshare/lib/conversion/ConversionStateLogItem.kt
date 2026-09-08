package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

sealed interface ConversionStateLogItem {
    val id: Int
    val state: ConversionState.HasDescription
    val startTimeMark: ComparableTimeMark

    data class Finished(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        override val startTimeMark: ComparableTimeMark,
        val elapsedTime: Duration,
        val succeeded: Boolean,
    ) : ConversionStateLogItem

    data class Pending(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        override val startTimeMark: ComparableTimeMark,
    ) : ConversionStateLogItem
}
