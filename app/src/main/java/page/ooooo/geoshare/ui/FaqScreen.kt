package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.openSettingsOpenByDefault
import page.ooooo.geoshare.data.android.openSettingsOpenByDefaultForPackage
import page.ooooo.geoshare.data.android.openUriInDefaultApp
import page.ooooo.geoshare.ui.components.ExpandablePane
import page.ooooo.geoshare.ui.components.FaqHowItWorksText
import page.ooooo.geoshare.ui.components.NavigationBackButton
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScreenshotOpenByDefault
import page.ooooo.geoshare.ui.components.ScreenshotOpenByDefaultMapApp
import page.ooooo.geoshare.ui.string.FormatArg
import page.ooooo.geoshare.ui.string.annotatedStringResource
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.defaultLinkStyles
import page.ooooo.geoshare.ui.theme.tertiaryLinkStyles

@Keep
enum class FaqItemId {
    HOW_IT_WORKS,
    OPEN_BY_DEFAULT,
    LOCATION_PERMISSION,
    NAME_ONLY,
    PRIVACY,
}

@Composable
fun FaqScreen(
    initialExpandedItemId: FaqItemId? = null,
    onBack: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId) -> Unit,
) {
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // Do nothing.
    }

    FaqScreen(
        initialExpandedItemId = initialExpandedItemId,
        onBack = onBack,
        onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen,
        onShowOpenByDefaultSettings = {
            context.openSettingsOpenByDefault(settingsLauncher)
        },
        onShowOpenByDefaultSettingsForPackage = { packageName ->
            context.openSettingsOpenByDefaultForPackage(settingsLauncher, packageName)
        }
    )
}

@Composable
private fun FaqScreen(
    initialExpandedItemId: FaqItemId? = null,
    onBack: () -> Unit,
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId) -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
    onShowOpenByDefaultSettingsForPackage: (packageName: String) -> Unit,
) {
    val spacing = LocalSpacing.current
    var expandedItemId by retain { mutableStateOf(initialExpandedItemId) }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.faq_title)) },
                navigationIcon = {
                    NavigationBackButton(onBack)
                },
            )
        },
    ) { innerPadding ->
        val context = LocalContext.current
        val appName = stringResource(R.string.app_name)
        val appServerName = stringResource(R.string.app_server_name)
        val appServerUrl = stringResource(R.string.app_server_url)
        LazyColumn(
            Modifier
                .widthIn(max = 600.dp)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .testTag("geoShareFaqPane"),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            item(key = "how_it_works", contentType = "faq_item") {
                FaqItem(
                    itemId = FaqItemId.HOW_IT_WORKS,
                    expandedItemId = expandedItemId,
                    onSetExpandedItemId = { expandedItemId = it },
                    title = stringResource(R.string.faq_how_headline),
                    modifier = Modifier.padding(top = spacing.small),
                ) {
                    FaqHowItWorksText(onNavigateToUserPreferencesScreen = onNavigateToUserPreferencesScreen)
                }
            }
            item(key = "how_it_works_divider", contentType = "faq_divider") {
                FaqDivider()
            }
            item(key = "name_only", contentType = "faq_item") {
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
            }
            item(key = "name_only_divider", contentType = "faq_divider") {
                FaqDivider()
            }
            item(key = "privacy", contentType = "faq_item") {
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
                            tertiaryLinkStyles(),
                            FormatArg.Text(appName),
                            FormatArg.Text(appServerName),
                            FormatArg.Link(appServerName) {
                                context.openUriInDefaultApp(appServerUrl)
                            },
                        )
                    )
                    ParagraphText(
                        stringResource(R.string.faq_privacy_text_html, appName)
                    )
                }
            }
            item(key = "privacy_divider", contentType = "faq_divider") {
                FaqDivider()
            }
            item(key = "location_permission", contentType = "faq_item") {
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
            item(key = "location_permission_divider", contentType = "faq_divider") {
                FaqDivider()
            }
            item(key = "open_by_default", contentType = "faq_item") {
                FaqItem(
                    itemId = FaqItemId.OPEN_BY_DEFAULT,
                    expandedItemId = expandedItemId,
                    onSetExpandedItemId = { expandedItemId = it },
                    title = stringResource(R.string.faq_open_by_default_headline, appName),
                ) {
                    FaqFigure(
                        annotatedStringResource(
                            R.string.intro_open_by_default_google_maps_caption,
                            textLinkStyles = tertiaryLinkStyles(),
                            FormatArg.Text(
                                stringResource(R.string.open_by_default),
                                style = SpanStyle(fontStyle = FontStyle.Italic),
                            ),
                        )
                    ) {
                        ScreenshotOpenByDefaultMapApp()
                        Button(
                            {
                                onShowOpenByDefaultSettingsForPackage(PackageNames.GOOGLE_MAPS)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                        ) {
                            Text(stringResource(R.string.intro_open_by_default_google_maps_button))
                        }
                    }
                    FaqFigure(
                        annotatedStringResource(
                            R.string.intro_open_by_default_app_caption,
                            textLinkStyles = tertiaryLinkStyles(),
                            FormatArg.Text(appName),
                            FormatArg.Text(
                                stringResource(R.string.open_by_default),
                                style = SpanStyle(fontStyle = FontStyle.Italic),
                            ),
                            FormatArg.Text(
                                stringResource(R.string.open_by_default_add_links),
                                style = SpanStyle(fontStyle = FontStyle.Italic),
                            ),
                        )
                    ) {
                        ScreenshotOpenByDefault()
                        FilledTonalButton(
                            {
                                onShowOpenByDefaultSettings()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                        ) {
                            Text(stringResource(R.string.intro_open_by_default_app_button, appName))
                        }
                        ParagraphText(
                            annotatedStringResource(
                                R.string.intro_open_by_default_app_note_1,
                                defaultLinkStyles(),
                                *listOf("maps.app.goo.gl", "maps.google.com", "www.google.com")
                                    .map { FormatArg.Text(it, SpanStyle(fontWeight = FontWeight.Bold)) }
                                    .toTypedArray(),
                            )
                        )
                        ParagraphText(
                            stringResource(R.string.intro_open_by_default_app_note)
                        )
                    }
                }
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
        modifier = modifier
            .padding(start = spacing.windowPadding, end = spacing.windowPadding - 4.dp)
            .testTag("geoShareFaqItem_$itemId"),
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

@Composable
private fun FaqDivider() {
    val spacing = LocalSpacing.current

    HorizontalDivider(Modifier.padding(horizontal = spacing.windowPadding))
}

@Composable
private fun FaqFigure(text: AnnotatedString, content: @Composable () -> Unit = {}) {
    val spacing = LocalSpacing.current

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.tiny),
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            ParagraphText(text, Modifier.fillMaxWidth())
            content()
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

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
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

@Preview(showBackground = true)
@Composable
private fun OpenByDefaultPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.OPEN_BY_DEFAULT,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOpenByDefaultPreview() {
    AppTheme {
        FaqScreen(
            initialExpandedItemId = FaqItemId.OPEN_BY_DEFAULT,
            onBack = {},
            onNavigateToUserPreferencesScreen = {},
        )
    }
}
