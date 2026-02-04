package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.TextList
import page.ooooo.geoshare.ui.components.TextListBullet
import page.ooooo.geoshare.ui.components.TextListItem
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

enum class FaqItemId {
    HOW_IT_WORKS,
    PRIVACY,
    LOCATION_PERMISSION,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    initialExpandedItemId: FaqItemId? = null,
    onBack: () -> Unit = {},
    onNavigateToUserPreferencesConnectionPermissionScreen: () -> Unit = {},
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
        val appName = stringResource(R.string.app_name)
        Column(
            Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive),
        ) {
            FaqItem(
                itemId = FaqItemId.HOW_IT_WORKS,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                stringResource(R.string.faq_how_it_works_headline),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_how_it_works_text_1, appName)
                )
                ParagraphText(
                    stringResource(R.string.faq_how_it_works_text_2, appName)
                )
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                        lineBreak = LineBreak.Paragraph,
                        hyphens = Hyphens.Auto,
                    )
                ) {
                    TextList(verticalSpace = spacing.tiny) {
                        listOf(
                            R.string.faq_how_it_works_text_2_item_http to R.string.faq_how_it_works_text_2_item_http_example,
                            R.string.faq_how_it_works_text_2_item_html to R.string.faq_how_it_works_text_2_item_html_example,
                            R.string.faq_how_it_works_text_2_item_web to null,
                        ).forEach { (textResId, exampleResId) ->
                            TextListItem(bullet = { TextListBullet() }) {
                                Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny / 2)) {
                                    Text(stringResource(textResId))
                                    exampleResId?.let { exampleResId ->
                                        Text(
                                            stringResource(R.string.example, stringResource(exampleResId)),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = FontStyle.Italic,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                ParagraphText(
                    stringResource(R.string.faq_how_it_works_text_3, appName)
                )
                ParagraphText(
                    buildAnnotatedString {
                        val parts = stringResource(R.string.faq_how_it_works_text_4).split($$"%1$s")
                        parts.forEachIndexed { i, part ->
                            append(part)
                            if (i < parts.size - 1) {
                                withLink(
                                    LinkAnnotation.Clickable(
                                        "preferences",
                                        styles = TextLinkStyles(
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                textDecoration = TextDecoration.Underline
                                            )
                                        ),
                                    ) {
                                        onNavigateToUserPreferencesConnectionPermissionScreen()
                                    }
                                ) {
                                    append(stringResource(R.string.faq_how_it_works_text_4_preferences_button))
                                }
                            }
                        }
                    }
                )
            }
            FaqItem(
                itemId = FaqItemId.PRIVACY,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                stringResource(R.string.faq_privacy_headline),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_privacy_text, appName)
                )
            }
            FaqItem(
                itemId = FaqItemId.LOCATION_PERMISSION,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                stringResource(R.string.faq_location_permission_headline),
            ) {
                ParagraphText(
                    stringResource(R.string.faq_location_permission_text, appName)
                )
            }
        }
    }
}

@Composable
fun FaqItem(
    itemId: FaqItemId,
    expandedItemId: FaqItemId?,
    onSetExpandedItemId: (FaqItemId?) -> Unit,
    headline: String,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current
    val expanded = itemId == expandedItemId
    Column(verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = spacing.tinyAdaptive)
                .clickable(
                    onClickLabel = stringResource(if (expanded) R.string.faq_item_collapse else R.string.faq_item_expand),
                    onClick = { onSetExpandedItemId(if (expanded) null else itemId) },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                headline,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(9f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.weight(1f),
            )
        }
        AnimatedVisibility(expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)) {
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
        FaqScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        FaqScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun HowItWorksPreview() {
    AppTheme {
        FaqScreen(initialExpandedItemId = FaqItemId.HOW_IT_WORKS)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkHowItWorksPreview() {
    AppTheme {
        FaqScreen(initialExpandedItemId = FaqItemId.HOW_IT_WORKS)
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyPreview() {
    AppTheme {
        FaqScreen(initialExpandedItemId = FaqItemId.PRIVACY)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPrivacyPreview() {
    AppTheme {
        FaqScreen(initialExpandedItemId = FaqItemId.PRIVACY)
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionPreview() {
    AppTheme {
        FaqScreen(initialExpandedItemId = FaqItemId.LOCATION_PERMISSION)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLocationPermissionPreview() {
    AppTheme {
        FaqScreen(initialExpandedItemId = FaqItemId.LOCATION_PERMISSION)
    }
}
