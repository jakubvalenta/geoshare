package page.ooooo.geoshare

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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.components.ParagraphHtml
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun FaqScreen(
    initialExpanded: Boolean = false,
    onNavigateToMainScreen: () -> Unit = {},
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.faq_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateToMainScreen) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_content_description)
                    )
                }
            },
        )
    }) { innerPadding ->
        val appName = stringResource(R.string.app_name)
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            FaqItem(
                stringResource(R.string.faq_how_it_works_headline),
                initialExpanded,
            ) {
                ParagraphHtml(
                    stringResource(
                        R.string.faq_how_it_works_text,
                        appName
                    )
                )
            }
        }
    }
}

@Composable
fun FaqItem(
    headline: String,
    initialExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = Spacing.tiny)
                .clickable(
                    onClickLabel = stringResource(if (expanded) R.string.faq_item_collapse else R.string.faq_item_expand),
                    onClick = { expanded = !expanded },
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
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
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
        FaqScreen(initialExpanded = true)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        FaqScreen(initialExpanded = true)
    }
}
