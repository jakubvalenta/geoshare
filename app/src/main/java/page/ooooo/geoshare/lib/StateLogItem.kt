package page.ooooo.geoshare.lib

import kotlin.time.ComparableTimeMark

data class StateLogItem<T>(
    val id: Int,
    val state: T,
    val start: ComparableTimeMark,
)
