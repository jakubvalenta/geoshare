package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun ResultTime(elapsedTime: Duration) {
    Text(
        elapsedTime.toString(DurationUnit.SECONDS, 2),
        style = MaterialTheme.typography.bodySmall,
    )
}
