package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultCard(
    geoUriApps: List<ConversionViewModel.App>,
    position: Position,
    onCopy: () -> Unit,
    onShare: (String) -> Unit,
    onSkip: () -> Unit,
) {
    Column {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(top = Spacing.small),
            shape = OutlinedTextFieldDefaults.shape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Row(
                Modifier.padding(
                    start = Spacing.small,
                    top = Spacing.tiny,
                    end = 4.dp,
                    bottom = Spacing.tiny,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(1f)) {
                    SelectionContainer {
                        Text(position.toDegString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                IconButton({ onCopy() }) {
                    Icon(
                        painterResource(R.drawable.content_copy_24px),
                        contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                    )
                }
            }
        }
        Row(
            Modifier
                .padding(top = Spacing.tiny)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            SuggestionChip(
                onClick = { onCopy() },
                label = {
                    Text(stringResource(R.string.conversion_succeeded_copy_geo))
                },
            )
        }

        Text(
            stringResource(R.string.conversion_succeeded_apps_headline),
            Modifier.padding(top = Spacing.medium, bottom = Spacing.small),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (geoUriApps.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                geoUriApps.map {
                    item(it.packageName) {
                        Column(
                            Modifier
                                .clickable { onShare(it.packageName) }
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                            Image(
                                rememberDrawablePainter(it.icon),
                                it.label,
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .widthIn(max = 48.dp),
                            )
                            Text(
                                it.label,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                stringResource(R.string.conversion_succeeded_apps_not_found),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
            )
        }
        Row(
            Modifier
                .padding(top = Spacing.small)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            SuggestionChip(
                onClick = { onSkip() },
                label = {
                    Text(stringResource(R.string.conversion_succeeded_skip))
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
                    ),
                ),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            ResultCard(
                geoUriApps = listOf(
                    ConversionViewModel.App(
                        BuildConfig.APPLICATION_ID,
                        "My Map App",
                        icon = context.getDrawable(R.drawable.ic_launcher_foreground)!!,
                    ),
                ),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoAppsPreview() {
    AppTheme {
        Surface {
            ResultCard(
                geoUriApps = listOf(),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNoAppsPreview() {
    AppTheme {
        Surface {
            ResultCard(
                geoUriApps = listOf(),
                position = Position("50.123456", "11.123456"),
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}
