package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

enum class FaqItemId {
    HOW_IT_WORKS,
    LOCATION_PERMISSION,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    initialExpandedItemId: FaqItemId? = null,
    onBack: () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    var expandedItemId by remember { mutableStateOf(initialExpandedItemId) }

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
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            FaqItem(
                itemId = FaqItemId.HOW_IT_WORKS,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                stringResource(R.string.faq_how_it_works_headline),
            ) {
                ParagraphHtml(
                    stringResource(R.string.faq_how_it_works_text, appName)
                )
            }
            FaqItem(
                itemId = FaqItemId.LOCATION_PERMISSION,
                expandedItemId = expandedItemId,
                onSetExpandedItemId = { expandedItemId = it },
                stringResource(R.string.faq_location_permission_headline),
            ) {
                ParagraphHtml(
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
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = spacing.tiny)
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
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
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
