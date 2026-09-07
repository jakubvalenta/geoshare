package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateHistoryItem
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.conversion.InputMatched
import page.ooooo.geoshare.lib.conversion.PermissionGranted
import page.ooooo.geoshare.lib.conversion.PermissionGrantedBasicInput
import page.ooooo.geoshare.lib.conversion.SourceReceived
import page.ooooo.geoshare.lib.inputs.BasicInput
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

fun LazyListScope.resultLoadingIndicator(stateHistory: List<ConversionStateHistoryItem>, onCancel: () -> Unit) {
    val stateHistoryPairs = stateHistory
        .zipWithNext()
        .run { stateHistory.lastOrNull()?.let { plus(it to null) } ?: this }
        .reversed()

    stateHistoryPairs.forEach { (item, nextItem) ->
        item {
            ResultLoadingIndicatorItem(item = item, nextItem = nextItem, onCancel = onCancel)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LazyItemScope.ResultLoadingIndicatorItem(
    item: ConversionStateHistoryItem,
    nextItem: ConversionStateHistoryItem? = null,
    iconSize: Dp = 46.dp,
    onCancel: () -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current
    val loadingIndicator = (item.conversionState as? ConversionState.HasLargeLoadingIndicator)
        ?.getLoadingIndicator(resources)

    var elapsed by remember { mutableStateOf(nextItem?.timeMark?.minus(item.timeMark) ?: Duration.ZERO) }

    LaunchedEffect(nextItem) {
        while (nextItem == null) {
            elapsed = item.timeMark.elapsedNow()
            delay(100.milliseconds)
        }
        nextItem.timeMark - item.timeMark
    }

    Column(
        Modifier
            .animateItem()
            .padding(vertical = spacing.extraTiny),
        verticalArrangement = Arrangement.spacedBy(spacing.tiny),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.extraTiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (nextItem == null) {
                LoadingIndicator(
                    Modifier.size(iconSize),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            } else if (nextItem.conversionState is ConversionState.HasAttempt && nextItem.conversionState.lastAttempt != null) {
                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.tertiary)
            } else {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.tertiary)
            }
            Text(
                loadingIndicator?.title
                    ?: (item.conversionState as? ConversionState.HasError)?.message
                    ?: item.conversionState.toString(),
                Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = if (nextItem == null) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
            )
            Text(
                "${elapsed.inWholeMilliseconds}ms",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (nextItem == null) {
            Column(Modifier.padding(start = iconSize + spacing.extraTiny)) {
                loadingIndicator?.description?.let { description ->
                    Text(
                        description,
                        Modifier.testTag("geoShareMainLoadingIndicatorDescription"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onCancel,
                    Modifier
                        .padding(vertical =spacing.small)
                        .testTag("geoShareMainLoadingIndicatorCancel"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(stringResource(R.string.conversion_loading_indicator_cancel))
                }
            }
            HorizontalDivider()
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val matchedInput: MatchedInput<BasicInput<Uri>> = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            )
            val permission = Permission.ALWAYS
            val timeSource = TestTimeSource()
            LazyColumn {
                resultLoadingIndicator(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            Initial,
                            timeSource.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            SourceReceived(source),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            InputMatched(source, matchedInput, permission),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGranted(source, matchedInput, permission, results = emptyMap()),
                            timeSource.apply { plusAssign(20.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput,
                                permission,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                    ),
                    onCancel = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val matchedInput: MatchedInput<BasicInput<Uri>> = MatchedInput(
                FakeInputRepository.googleMapsShortLinkInput, "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            )
            val permission = Permission.ALWAYS
            val timeSource = TestTimeSource()
            LazyColumn {
                resultLoadingIndicator(
                    stateHistory = listOf(
                        ConversionStateHistoryItem(
                            Initial,
                            timeSource.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            SourceReceived(source),
                            timeSource.apply { plusAssign(30.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            InputMatched(source, matchedInput, permission),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGranted(source, matchedInput, permission, results = emptyMap()),
                            timeSource.apply { plusAssign(20.milliseconds) }.markNow(),
                        ),
                        ConversionStateHistoryItem(
                            PermissionGrantedBasicInput(
                                source,
                                matchedInput,
                                permission,
                                results = emptyMap(),
                                lastAttempt = Attempt(2, ConnectTimeoutNetworkException(Exception())),
                            ),
                            timeSource.apply { plusAssign(100.milliseconds) }.markNow(),
                        ),
                    ),
                    onCancel = {},
                )
            }
        }
    }
}
