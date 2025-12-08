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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.inputs.GeoUriInput
import page.ooooo.geoshare.lib.inputs.InputDocumentation
import page.ooooo.geoshare.lib.inputs.InputDocumentationItem
import page.ooooo.geoshare.lib.inputs.OpenStreetMapInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private data class DocumentationInputDetails(
    val documentationInput: InputDocumentationItem,
    val defaultHandlerEnabled: Boolean?,
)

private fun getDocumentationInputDetails(
    documentation: InputDocumentation,
    packageManager: PackageManager,
): List<DocumentationInputDetails> =
    documentation.items.map { documentationInput ->
        DocumentationInputDetails(
            documentationInput,
            if (documentationInput is InputDocumentationItem.Url) {
                AndroidTools.isDefaultHandlerEnabled(packageManager, documentationInput.urlString)
            } else {
                null
            },
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsDetailPane(
    currentDocumentation: InputDocumentation,
    expanded: Boolean,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var documentationInputInfos by remember {
        mutableStateOf(getDocumentationInputDetails(currentDocumentation, context.packageManager))
    }
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
                IconButton(onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_content_description)
                    )
                }
            }
        },
    )
    Column(Modifier.widthIn(max = 600.dp)) {
        SelectionContainer {
            Headline(stringResource(nameResId))
        }
        InputsHeader(
            text = stringResource(R.string.inputs_detail_text, appName),
            onShowOpenByDefaultSettings = onShowOpenByDefaultSettings,
        )
        Column(Modifier.padding(start = spacing.windowPadding, top = spacing.small, end = spacing.windowPadding)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.medium, bottom = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.inputs_link),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    stringResource(R.string.inputs_default_handler, appName),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            HorizontalDivider()
            Column(
                Modifier.verticalScroll(rememberScrollState()),
            ) {
                documentationInputDetails.forEach { documentationInputDetails ->
                    Row(
                        Modifier.padding(vertical = spacing.small),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionContainer(Modifier.weight(1f)) {
                            Text(
                                when (documentationInputDetails.documentationInput) {
                                    is InputDocumentationItem.Text ->
                                        documentationInputDetails.documentationInput.text()

                                    is InputDocumentationItem.Url ->
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
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    HorizontalDivider()
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
            Column {
                InputsDetailPane(
                    nameResId = OpenStreetMapInput.documentation.nameResId,
                    documentationInputDetails = OpenStreetMapInput.documentation.items.mapIndexed { i, documentationInput ->
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
                    documentationInputDetails = OpenStreetMapInput.documentation.items.mapIndexed { i, documentationInput ->
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
                    documentationInputDetails = GeoUriInput.documentation.items.mapIndexed { i, documentationInput ->
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
                    documentationInputDetails = GeoUriInput.documentation.items.mapIndexed { i, documentationInput ->
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

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        Surface {
            Column {
                InputsDetailPane(
                    nameResId = OpenStreetMapInput.documentation.nameResId,
                    documentationInputDetails = OpenStreetMapInput.documentation.items.mapIndexed { i, documentationInput ->
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
