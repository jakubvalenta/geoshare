package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark

data class ConversionStateLogItem(
    val id: Int,
    val state: ConversionState,
    val startTimeMark: ComparableTimeMark,
)

sealed interface ExtendedConversionStateLogItem {
    val id: Int
    val state: ConversionState.HasDescription

    data class Finished(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        val startTimeMark: ComparableTimeMark,
        val endTimeMark: ComparableTimeMark,
        val succeeded: Boolean,
    ) : ExtendedConversionStateLogItem

    data class Pending(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        val startTimeMark: ComparableTimeMark,
    ) : ExtendedConversionStateLogItem
}
