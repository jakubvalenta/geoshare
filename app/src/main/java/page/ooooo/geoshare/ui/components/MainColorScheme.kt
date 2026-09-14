package page.ooooo.geoshare.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import page.ooooo.geoshare.lib.conversion.ConversionState

@Composable
fun mainContainerColor(currentState: ConversionState): Color =
    when (currentState) {
        is ConversionState.HasError if currentState.warning -> MaterialTheme.colorScheme.surfaceContainerHighest
        is ConversionState.HasError -> MaterialTheme.colorScheme.errorContainer
        is ConversionState.HasResult -> MaterialTheme.colorScheme.secondaryContainer
        is ConversionState.HasDescription -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surface
    }
