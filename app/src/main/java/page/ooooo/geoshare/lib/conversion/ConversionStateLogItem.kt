package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark

sealed interface ConversionStateLogItem {
    val id: Int
    val state: ConversionState.HasDescription

    data class Finished(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        val start: ComparableTimeMark,
        val end: ComparableTimeMark,
        val succeeded: Boolean,
    ) : ConversionStateLogItem

    data class Pending(
        override val id: Int,
        override val state: ConversionState.HasDescription,
        val start: ComparableTimeMark,
    ) : ConversionStateLogItem
}
