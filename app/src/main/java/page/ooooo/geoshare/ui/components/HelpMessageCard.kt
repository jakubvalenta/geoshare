package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.data.local.preferences.isDismissed
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun HelpMessageCard(
    helpMessage: HelpMessage,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    after: (@Composable () -> Unit)? = null,
    actionText: (@Composable () -> String)? = null,
    onAction: () -> Unit = {},
    onDismiss: (helpMessage: HelpMessage) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current

    val dismissedHelpMessages by dismissedHelpMessages.collectAsStateWithLifecycle()
    val visible = remember(dismissedHelpMessages) { !helpMessage.isDismissed(dismissedHelpMessages) }

    AnimatedVisibility(
        visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Card(
            modifier = modifier
                .widthIn(max = spacing.largeButtonMaxWidth + 2 * spacing.largeButtonHorizontalPadding)
                .testTag("geoShareHelpMessage_$helpMessage"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        ) {
            Box {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(spacing.small),
                    verticalArrangement = Arrangement.spacedBy(spacing.tiny),
                ) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)) {
                            title()
                        }
                        content()
                        actionText?.invoke()?.let { actionText ->
                            Text(
                                buildAnnotatedString {
                                    ClickableLink(
                                        actionText,
                                        styles = AnnotatedString.UnderlinedLinkStyles,
                                        onClick = onAction,
                                    )
                                }
                            )
                        }
                        after?.invoke()
                    }
                }
                IconButton(
                    { onDismiss(helpMessage) },
                    Modifier
                        .align(Alignment.TopEnd)
                        .testTag("geoShareHelpMessageDismiss"),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.intro_nav_close),
                    )
                }
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
            HelpMessageCard(
                helpMessage = HelpMessage.SHARE_SOURCE,
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                title = { Text(stringResource(R.string.help_share_source_title)) },
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                onAction = {},
                onDismiss = {},
            ) {
                val shareIconId = "shareIcon"
                val shareIconSize = 14.sp
                ParagraphText(
                    annotatedStringResource(
                        R.string.help_share_source_text,
                        FormatArg.InlineContent(shareIconId),
                        FormatArg.Text(stringResource(R.string.app_name)),
                    ),
                    inlineContent = mapOf(
                        shareIconId to InlineTextContent(
                            Placeholder(
                                width = 14.sp,
                                height = 14.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                            )
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                Modifier.requiredSize(with(LocalDensity.current) { shareIconSize.toDp() }),
                            )
                        }
                    )
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
            HelpMessageCard(
                helpMessage = HelpMessage.SHARE_SOURCE,
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                title = { Text(stringResource(R.string.help_share_source_title)) },
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                onAction = {},
                onDismiss = {},
            ) {
                val shareIconId = "shareIcon"
                val shareIconSize = 14.sp
                ParagraphText(
                    annotatedStringResource(
                        R.string.help_share_source_text,
                        FormatArg.InlineContent(shareIconId),
                        FormatArg.Text(stringResource(R.string.app_name)),
                    ),
                    inlineContent = mapOf(
                        shareIconId to InlineTextContent(
                            Placeholder(
                                width = 14.sp,
                                height = 14.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                            )
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                Modifier.requiredSize(with(LocalDensity.current) { shareIconSize.toDp() }),
                            )
                        }
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        Surface {
            HelpMessageCard(
                helpMessage = HelpMessage.SHARE_SOURCE,
                dismissedHelpMessages = MutableStateFlow(emptySet()),
                title = { Text(stringResource(R.string.help_share_source_title)) },
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                onAction = {},
                onDismiss = {},
            ) {
                val shareIconId = "shareIcon"
                val shareIconSize = 14.sp
                ParagraphText(
                    annotatedStringResource(
                        R.string.help_share_source_text,
                        FormatArg.InlineContent(shareIconId),
                        FormatArg.Text(stringResource(R.string.app_name)),
                    ),
                    inlineContent = mapOf(
                        shareIconId to InlineTextContent(
                            Placeholder(
                                width = 14.sp,
                                height = 14.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                            )
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                Modifier.requiredSize(with(LocalDensity.current) { shareIconSize.toDp() }),
                            )
                        }
                    )
                )
            }
        }
    }
}
