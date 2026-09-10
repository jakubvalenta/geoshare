package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark

sealed interface ConversionStateLogItem {
    val id: Int
    val state: ConversionState.HasDescription
    val startTimeMark: ComparableTimeMark

    data class Finished(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        override val startTimeMark: ComparableTimeMark,
        val endTimeMark: ComparableTimeMark,
        val succeeded: Boolean,
    ) : ConversionStateLogItem

    data class Pending(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        override val startTimeMark: ComparableTimeMark,
    ) : ConversionStateLogItem
}
