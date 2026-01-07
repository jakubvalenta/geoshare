package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FeatureBadged(
    enabled: Boolean,
    badge: @Composable (modifier: Modifier) -> Unit,
    content: @Composable (modifier: Modifier) -> Unit,
) {
    if (enabled) {
        Box(contentAlignment = Alignment.CenterEnd) {
            content(Modifier.padding(end = 17.dp))
            badge(Modifier.offset(y = (-13).dp))
        }
    } else {
        content(Modifier)
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(top = 10.dp)) {
                FeatureBadged(
                    enabled = true,
                    badge = { modifier ->
                        FeatureBadgeSmall({}, modifier)
                    },
                ) { modifier ->
                    Button(
                        {},
                        modifier,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(stringResource(R.string.user_preferences_automation_title))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(top = 10.dp)) {
                FeatureBadged(
                    enabled = true,
                    badge = { modifier ->
                        FeatureBadgeSmall({}, modifier)
                    },
                ) { modifier ->
                    Button(
                        {},
                        modifier,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(stringResource(R.string.user_preferences_automation_title))
                    }
                }
            }
        }
    }
}
