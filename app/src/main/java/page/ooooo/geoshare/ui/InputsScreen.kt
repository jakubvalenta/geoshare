package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.lib.inputs.InputDocumentationId
import page.ooooo.geoshare.lib.inputs.allInputs
import page.ooooo.geoshare.ui.components.BasicListDetailScaffold
import page.ooooo.geoshare.ui.components.InputsDetailPane
import page.ooooo.geoshare.ui.components.InputsListPane
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsScreen(
    initialDocumentationId: InputDocumentationId?,
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val changelogShownForVersionCode by viewModel.changelogShownForVersionCode.collectAsStateWithLifecycle()

    InputsScreen(
        changelogShownForVersionCode = changelogShownForVersionCode,
        initialDocumentationId = initialDocumentationId,
        onBack = {
            viewModel.setChangelogShown()
            onBack()
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InputsScreen(
    changelogShownForVersionCode: Int?,
    initialDocumentationId: InputDocumentationId?,
    onBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = listOf(
            if (initialDocumentationId == null) {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)
            } else {
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, initialDocumentationId)
            },
        ),
    )
    val documentations = allInputs.map { input -> input.documentation }
    val currentDocumentation = remember(navigator.currentDestination, documentations) {
        navigator.currentDestination?.contentKey?.let { id -> documentations.find { it.id == id } }
    }

    BackHandler {
        onBack()
    }

    BasicListDetailScaffold(
        navigator = navigator,
        listPane = { wide, containerColor ->
            InputsListPane(
                currentDocumentation = currentDocumentation,
                documentations = documentations,
                changelogShownForVersionCode = changelogShownForVersionCode,
                containerColor = containerColor,
                wide = wide,
                onBack = {
                    coroutineScope.launch {
                        if (navigator.canNavigateBack()) {
                            navigator.navigateBack()
                        } else {
                            onBack()
                        }
                    }
                },
                onNavigateToDocumentation = { id ->
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                },
            )
        },
        detailPane = { wide ->
            if (currentDocumentation != null) {
                InputsDetailPane(
                    currentDocumentation = currentDocumentation,
                    wide = wide,
                    onBack = {
                        coroutineScope.launch {
                            if (navigator.canNavigateBack()) {
                                navigator.navigateBack()
                            } else {
                                onBack()
                            }
                        }
                    },
                )
            }
        },
        listContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.GOOGLE_MAPS,
                    changelogShownForVersionCode = 25,
                    onBack = {},
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
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.GOOGLE_MAPS,
                    changelogShownForVersionCode = 25,
                    onBack = {},
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
                InputsScreen(
                    initialDocumentationId = InputDocumentationId.GOOGLE_MAPS,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = null,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = null,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletEmptyPreview() {
    AppTheme {
        Surface {
            Column {
                InputsScreen(
                    initialDocumentationId = null,
                    changelogShownForVersionCode = 25,
                    onBack = {},
                )
            }
        }
    }
}
