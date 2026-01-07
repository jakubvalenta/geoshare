package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun BoxScope.FeatureBadgeLarge(onNavigateToSubscriptionScreen: () -> Unit) {
    val appName = stringResource(R.string.app_name)

    ElevatedCard(
        Modifier.align(Alignment.Center),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
    ) {
        Row(
            Modifier.padding(
                start = 18.dp,
                top = 5.dp,
                end = 9.dp,
                bottom = 5.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.crown_24px), null)
            Text(
                stringResource(R.string.feature_badge_text, appName),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onNavigateToSubscriptionScreen) {
                Text(stringResource(R.string.feature_badge_button))
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Box {
                FeatureBadgeLarge(onNavigateToSubscriptionScreen = {})
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Box {
                FeatureBadgeLarge(onNavigateToSubscriptionScreen = {})
            }
        }
    }
}
