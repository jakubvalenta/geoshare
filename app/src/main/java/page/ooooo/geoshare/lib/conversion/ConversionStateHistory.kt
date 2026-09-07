package page.ooooo.geoshare.lib.conversion

import kotlin.time.ComparableTimeMark

data class ConversionStateHistoryItem(
    val conversionState: ConversionState.HasDescription,
    val timeMark: ComparableTimeMark,
)

typealias ConversionStateHistory = List<ConversionStateHistoryItem>
