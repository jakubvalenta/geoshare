package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.ui.components.ExpandablePane
import page.ooooo.geoshare.ui.components.FormatArg
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.TextList
import page.ooooo.geoshare.ui.components.TextListBullet
import page.ooooo.geoshare.ui.components.TextListItem
import page.ooooo.geoshare.ui.components.annotatedStringResource
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Keep
enum class FaqItemId {
    HOW_IT_WORKS,
    LOCATION_PERMISSION,
    NAME_ONLY,
    PRIVACY,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    initialExpandedItemId: FaqItemId? = null,
    onBack: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId) -> Unit,
) {
    val spacing = LocalSpacing.current
    var expandedItemId by retain { mutableStateOf(initialExpandedItemId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.faq_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back_content_description)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val context = LocalContext.current
        val appName = stringResource(R.string.app_name)
        val appServerName = stringResource(R.string.app_server_name)
        val appServerUrl = stringResource(R.string.app_server_url)
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            FaqItem(
                itemId = FaqItemId.HOW_IT_WORKS,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                title = stringResource(R.string.faq_how_headline),
                modifier = Modifier.padding(top = spacing.small),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_how_text_1, appName)
                )
                ParagraphText(
                    stringResource(R.string.faq_how_text_2, appName)
                )
                TextList(verticalSpace = spacing.tiny) {
                    listOf(
                        R.string.faq_how_text_2_item_http to R.string.faq_how_text_2_item_http_example,
                        R.string.faq_how_text_2_item_html to R.string.faq_how_text_2_item_html_example,
                        R.string.faq_how_text_2_item_api to R.string.faq_how_text_2_item_api_example,
                        R.string.faq_how_text_2_item_web to null,
                    ).forEach { (textResId, exampleResId) ->
                        TextListItem(bullet = { TextListBullet() }) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny / 2)) {
                                Text(stringResource(textResId))
                                exampleResId?.let { exampleResId ->
                                    Text(
                                        stringResource(R.string.example, stringResource(exampleResId)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = FontStyle.Italic,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
                ParagraphText(
                    stringResource(R.string.faq_how_text_3, appName)
                )
                ParagraphText(
                    annotatedStringResource(
                        R.string.faq_how_text_4,
                        FormatArg.Link(stringResource(R.string.faq_how_text_4_preferences_button)) {
                            onNavigateToUserPreferencesScreen(UserPreferenceGroupId.CONNECTION_PERMISSION)
                        },
                    )
                )
            }
            FaqItem(
                itemId = FaqItemId.NAME_ONLY,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                title = stringResource(R.string.faq_name_only_title),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_name_only_text_1, appName)
                )
                ParagraphText(
                    stringResource(R.string.faq_name_only_text_2)
                )
            }
            FaqItem(
                itemId = FaqItemId.PRIVACY,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                title = stringResource(R.string.faq_privacy_headline),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_privacy_text, appName)
                )
                ParagraphText(
                    annotatedStringResource(
                        R.string.faq_privacy_text_server,
                        FormatArg.Text(appName),
                        FormatArg.Text(appServerName),
                        FormatArg.Link(appServerName) {
                            AndroidTools.openWebUri(context, appServerUrl)
                        },
                    )
                )
                ParagraphText(
                    stringResource(R.string.faq_privacy_text_html, appName)
                )
            }
            FaqItem(
                itemId = FaqItemId.LOCATION_PERMISSION,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                title = stringResource(R.string.faq_location_permission_headline),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_location_permission_text, appName)
                )
            }
        }
    }
}

@Composable
private fun FaqItem(
    itemId: FaqItemId,
    expandedItemId: FaqItemId?,
    onSetExpandedItemId: (FaqItemId?) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current

    ExpandablePane(
        expanded = expandedItemId == itemId,
        onSetExpanded = { onSetExpandedItemId(if (expandedItemId != itemId) itemId else null) },
        title = {
            Text(
                title,
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        modifier = modifier.padding(start = spacing.windowPadding, end = spacing.windowPadding - 4.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            Modifier
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph,
                    hyphens = Hyphens.Auto,
                )
            ) {
                content()
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        FaqScreen(
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        FaqScreen(
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HowItWorksPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.HOW_IT_WORKS,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkHowItWorksPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.HOW_IT_WORKS,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NameOnlyPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.NAME_ONLY,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkNameOnlyPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.NAME_ONLY,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.PRIVACY,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPrivacyPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.PRIVACY,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.LOCATION_PERMISSION,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLocationPermissionPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.LOCATION_PERMISSION,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}
