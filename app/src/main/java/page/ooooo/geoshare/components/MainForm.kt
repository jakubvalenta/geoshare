package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainForm(
    inputUriString: String,
    onStart: () -> Unit,
    onUpdateInput: (String) -> Unit,
    onNavigateToUserPreferencesScreen: () -> Unit,
    onNavigateToFaqScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(appName) },
                actions = {
                    Box {
                        IconButton(
                            { menuExpanded = true },
                            Modifier.padding(end = Spacing.windowPadding - Spacing.builtInTopBarPaddingEnd),
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.nav_menu_content_description),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.user_preferences_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToUserPreferencesScreen()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.faq_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToFaqScreen()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.intro_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToIntroScreen()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about_title)) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToAboutScreen()
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = Spacing.windowPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = inputUriString,
                onValueChange = { onUpdateInput(it) },
                modifier = Modifier
                    .testTag("geoShareMainInputUriStringTextField")
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
                label = {
                    Text(
                        stringResource(R.string.main_input_uri_label),
                    )
                },
                trailingIcon = if (inputUriString.isNotEmpty()) {
                    {
                        IconButton({ onUpdateInput("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.main_input_uri_clear_content_description),
                            )
                        }
                    }
                } else {
                    null
                },
            )
            Button(
                { onStart() },
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.small),
            ) {
                Text(stringResource(R.string.main_create_geo_uri))
            }
            TextButton(
                { onNavigateToIntroScreen() },
                Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.main_navigate_to_intro, appName))
            }
            Card(
                Modifier.padding(top = Spacing.medium),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            ) {
                Row(
                    Modifier.padding(Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.lightbulb_24px), null)
                    Text(
                        AnnotatedString.fromHtml(
                            stringResource(R.string.main_info_box, appName),
                            linkStyles = TextLinkStyles(
                                SpanStyle(textDecoration = TextDecoration.Underline)
                            ),
                        ),
                        style = MaterialTheme.typography.bodySmall,
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
        MainForm(
            inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            onStart = {},
            onUpdateInput = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToAboutScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainForm(
            inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
            onStart = {},
            onUpdateInput = {},
            onNavigateToUserPreferencesScreen = {},
            onNavigateToFaqScreen = {},
            onNavigateToIntroScreen = {},
            onNavigateToAboutScreen = {},
        )
    }
}
