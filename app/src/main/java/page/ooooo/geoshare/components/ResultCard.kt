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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultCard(
    geoUriApps: List<ConversionViewModel.App>,
    geoUri: String,
    onCopy: () -> Unit,
    onShare: (String) -> Unit,
    onSkip: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Card(
            Modifier.fillMaxWidth(),
            shape = OutlinedTextFieldDefaults.shape,
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Row(Modifier.padding(Spacing.small)) {
                SelectionContainer {
                    Text(geoUri, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            AssistChip(
                onClick = { onCopy() },
                label = { Text(stringResource(R.string.main_result_geo_uri_copy_content_description)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.content_copy_24px),
                        contentDescription = null,
                    )
                })
            AssistChip(onClick = { onSkip() }, label = { Text(stringResource(R.string.skip_activity)) }, leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                )
            })
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            geoUriApps.map {
                item(it.packageName) {
                    Column(
                        Modifier
                            .clickable { onShare(it.packageName) }
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                        Image(
                            rememberDrawablePainter(it.icon),
                            it.label,
                            Modifier.widthIn(max = 64.dp),
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
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            ResultCard(
                geoUriApps = listOf(),
                geoUri = "geo:50.123456,11.123456",
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
            ResultCard(
                geoUriApps = listOf(),
                geoUri = "geo:50.123456,11.123456",
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}
