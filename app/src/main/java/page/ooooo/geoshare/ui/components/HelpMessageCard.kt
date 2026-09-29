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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun HelpMessageCard(
    title: @Composable () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
    after: (@Composable () -> Unit)? = null,
    actionText: (@Composable () -> String)? = null,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    onAction: () -> Unit = {},
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current

    AnimatedVisibility(
        visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Card(
            modifier = modifier.widthIn(max = spacing.largeButtonMaxWidth + 2 * spacing.largeButtonHorizontalPadding),
            colors = colors,
        ) {
            Box(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(
                        start = spacing.small,
                        top = spacing.small,
                        end = if (onDismissRequest != null) 46.dp else 0.dp,
                        bottom = spacing.tiny,
                    ),
                ) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)) {
                            title()
                        }
                    }
                }
                if (onDismissRequest != null) {
                    IconButton(
                        onDismissRequest,
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
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.small)
                    .padding(bottom = spacing.small),
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
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
                title = { Text(stringResource(R.string.help_share_source_title)) },
                visible = true,
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                onAction = {},
                onDismissRequest = {},
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
                                width = shareIconSize,
                                height = shareIconSize,
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
                title = { Text(stringResource(R.string.help_share_source_title)) },
                visible = true,
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                onAction = {},
                onDismissRequest = {},
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
                                width = shareIconSize,
                                height = shareIconSize,
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

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface {
            HelpMessageCard(
                title = { Text(stringResource(R.string.help_share_source_title)) },
                visible = true,
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                onAction = {},
                onDismissRequest = null,
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
                                width = shareIconSize,
                                height = shareIconSize,
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
                title = { Text("Kotlin is a modern language that's concise, multiplatform, and interoperable with Java and other languages.") },
                visible = true,
                actionText = { stringResource(R.string.help_share_source_action, "OsmAnd") },
                onAction = {},
                onDismissRequest = {},
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
                                width = shareIconSize,
                                height = shareIconSize,
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
