package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@Composable
fun ResultCard(
    geoUriApps: List<Pair<String, Int>>,
    resultGeoUri: String,
    resultErrorMessageResId: Int?,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Card(
            Modifier.fillMaxWidth(),
            shape = OutlinedTextFieldDefaults.shape,
            colors = CardDefaults.cardColors(
                containerColor = if (resultErrorMessageResId == null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
                contentColor = if (resultErrorMessageResId == null) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
            ),
        ) {
            Row(Modifier.padding(Spacing.small)) {
                SelectionContainer {
                    Text(
                        if (resultErrorMessageResId == null) {
                            resultGeoUri
                        } else {
                            stringResource(resultErrorMessageResId)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
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
            AssistChip(
                onClick = { onShare() },
                label = { Text(stringResource(R.string.main_result_geo_uri_share_content_description)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
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
        FlowRow {
            for ((label, icon) in geoUriApps) {
                Column {
                    Text(label)
                    Icon(painterResource(icon), label)
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
                resultGeoUri = "geo:50.123456,11.123456",
                resultErrorMessageResId = null,
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
                resultGeoUri = "geo:50.123456,11.123456",
                resultErrorMessageResId = null,
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultErrorPreview() {
    AppTheme {
        Surface {
            ResultCard(
                geoUriApps = listOf(),
                resultGeoUri = "geo:50.123456,11.123456",
                resultErrorMessageResId = R.string.conversion_failed_unshorten_error,
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface {
            ResultCard(
                geoUriApps = listOf(),
                resultGeoUri = "geo:50.123456,11.123456",
                resultErrorMessageResId = R.string.conversion_failed_unshorten_error,
                onCopy = {},
                onShare = {},
                onSkip = {},
            )
        }
    }
}
