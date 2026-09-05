package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark

data class ConversionStateHistoryItem(
    val conversionState: ConversionState,
    val timeMark: ComparableTimeMark,
)

typealias ConversionStateHistory = List<ConversionStateHistoryItem>
