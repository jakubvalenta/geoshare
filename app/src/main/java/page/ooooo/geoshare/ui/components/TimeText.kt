package page.ooooo.geoshare.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun ElapsedTimeText(start: ComparableTimeMark) {
    var elapsedTime by remember { mutableStateOf(start.elapsedNow()) }

    LaunchedEffect(start) {
        while (true) {
            elapsedTime = start.elapsedNow()
            delay(100.milliseconds)
        }
    }

    SecondsTimeText(elapsedTime)
}

@Composable
fun SecondsTimeText(time: Duration) {
    Text(time.toString(DurationUnit.SECONDS, if (time < 1.seconds) 2 else 0))
}
