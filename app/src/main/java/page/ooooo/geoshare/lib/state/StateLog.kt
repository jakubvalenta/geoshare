package page.ooooo.geoshare.lib.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource

data class StateLogItem<S>(
    val id: Int,
    val state: S,
    val start: ComparableTimeMark,
)

typealias StateLog<S> = List<StateLogItem<S>>

fun <S> MutableStateFlow<StateLog<S>>.append(
    state: S,
    clear: Boolean = false,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) {
    update { prevLog ->
        val newLogItem = StateLogItem(prevLog.size, state, timeSource.markNow())
        if (clear) {
            listOf(newLogItem)
        } else {
            prevLog + newLogItem
        }
    }
}

sealed interface ExtendedStateLogItem<S> {
    val id: Int
    val state: S

    data class Finished<S>(
        override val id: Int,
        override val state: S,
        val start: ComparableTimeMark,
        val end: ComparableTimeMark,
        val succeeded: Boolean,
    ) : ExtendedStateLogItem<S>

    data class Pending<S>(
        override val id: Int,
        override val state: S,
        val start: ComparableTimeMark,
    ) : ExtendedStateLogItem<S>
}

typealias ExtendedStateLog<S> = List<ExtendedStateLogItem<S>>
