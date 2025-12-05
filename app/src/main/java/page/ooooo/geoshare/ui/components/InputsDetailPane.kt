package page.ooooo.geoshare.ui.components

import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.inputs.OpenStreetMapInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private data class DocumentationInputDetails(
    val documentationInput: Input.DocumentationInput,
    val defaultHandlerEnabled: Boolean?,
)

private fun getDocumentationInputDetails(
    documentation: Input.Documentation,
    packageManager: PackageManager,
): List<DocumentationInputDetails> =
    documentation.inputs.map { documentationInput ->
        DocumentationInputDetails(
            documentationInput,
            if (documentationInput is Input.DocumentationInput.Url) {
                AndroidTools.isDefaultHandlerEnabled(packageManager, documentationInput.urlString)
            } else {
                null
            },
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsDetailPane(
    currentDocumentation: Input.Documentation,
    expanded: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var documentationInputInfos = getDocumentationInputDetails(currentDocumentation, context.packageManager)
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        documentationInputInfos = getDocumentationInputDetails(currentDocumentation, context.packageManager)
    }

    InputsDetailPane(
        nameResId = currentDocumentation.nameResId,
        documentationInputDetails = documentationInputInfos,
        expanded = expanded,
        onBack = onBack,
        onShowOpenByDefaultSettings = { AndroidTools.showOpenByDefaultSettings(context, settingsLauncher) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputsDetailPane(
    @StringRes nameResId: Int,
    documentationInputDetails: List<DocumentationInputDetails>,
    expanded: Boolean,
    onBack: () -> Unit,
    onShowOpenByDefaultSettings: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    TopAppBar(
        title = {},
        navigationIcon = {
            if (expanded) {
                IconButton(onBack, Modifier.testTag("geoShareInputsBack")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_content_description)
                    )
                }
            }
        },
    )
    SelectionContainer {
        Headline(stringResource(nameResId))
    }
    Column(
        Modifier.padding(horizontal = spacing.windowPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Button({ onShowOpenByDefaultSettings() }) {
            Text(stringResource(R.string.url_converters_settings_button))
        }
        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.url_converters_url_header),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(R.string.url_converters_default_handler_header, appName),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            HorizontalDivider()
            documentationInputDetails.forEach { documentationInputDetails ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SelectionContainer(Modifier.weight(1f)) {
                        Text(
                            when (documentationInputDetails.documentationInput) {
                                is Input.DocumentationInput.Text ->
                                    documentationInputDetails.documentationInput.text()

                                is Input.DocumentationInput.Url ->
                                    documentationInputDetails.documentationInput.urlString
                                        .removePrefix("https://")
                                        .trimEnd('/')
                            },
                            Modifier.padding(end = spacing.tiny),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        stringResource(
                            when (documentationInputDetails.defaultHandlerEnabled) {
                                true -> R.string.yes
                                false -> R.string.no
                                null -> R.string.not_available
                            },
                            appName,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                HorizontalDivider()
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
            Column {
                InputsDetailPane(
                    nameResId = OpenStreetMapInput.documentation.nameResId,
                    documentationInputDetails = OpenStreetMapInput.documentation.inputs.mapIndexed { i, documentationInput ->
                        DocumentationInputDetails(
                            documentationInput,
                            i and 1 == 0,
                        )
                    },
                    expanded = true,
                    onBack = {},
                    onShowOpenByDefaultSettings = {},
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
            Column {
                InputsDetailPane(
                    nameResId = OpenStreetMapInput.documentation.nameResId,
                    documentationInputDetails = OpenStreetMapInput.documentation.inputs.mapIndexed { i, documentationInput ->
                        DocumentationInputDetails(
                            documentationInput,
                            i and 1 == 0,
                        )
                    },
                    expanded = true,
                    onBack = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextInputPreview() {
    AppTheme {
        Surface {
            Column {
                InputsDetailPane(
                    nameResId = GeoUriInput.documentation.nameResId,
                    documentationInputDetails = GeoUriInput.documentation.inputs.mapIndexed { i, documentationInput ->
                        DocumentationInputDetails(
                            documentationInput,
                            i and 1 == 0,
                        )
                    },
                    expanded = true,
                    onBack = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkTextInputPreview() {
    AppTheme {
        Surface {
            Column {
                InputsDetailPane(
                    nameResId = GeoUriInput.documentation.nameResId,
                    documentationInputDetails = GeoUriInput.documentation.inputs.mapIndexed { i, documentationInput ->
                        DocumentationInputDetails(
                            documentationInput,
                            i and 1 == 0,
                        )
                    },
                    expanded = true,
                    onBack = {},
                    onShowOpenByDefaultSettings = {},
                )
            }
        }
    }
}
