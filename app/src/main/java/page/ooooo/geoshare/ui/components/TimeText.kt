package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@Composable
fun ElapsedTimeText(startTimeMark: StateFlow<ComparableTimeMark?>) {
    val startTimeMark by startTimeMark.collectAsStateWithLifecycle()

    var elapsedTime by remember { mutableStateOf(startTimeMark?.elapsedNow() ?: Duration.ZERO) }

    LaunchedEffect(startTimeMark) {
        startTimeMark?.let { startTime ->
            while (true) {
                elapsedTime = startTime.elapsedNow()
                delay(100.milliseconds)
            }
        }
    }

    SecondsTimeText(elapsedTime)
}

@Composable
fun SecondsTimeText(time: Duration) {
    Text(time.toString(DurationUnit.SECONDS, 2), style = MaterialTheme.typography.bodySmall)
}
