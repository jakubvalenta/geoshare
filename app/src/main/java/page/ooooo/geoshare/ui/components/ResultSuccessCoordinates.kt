package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.outputs.*
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultSuccessCoordinates(
    outputs: List<Output>,
    onRun: (action: Action) -> Unit,
) {
    val spacing = LocalSpacing.current
    val (sheetVisible, setSheetVisible) = remember { mutableStateOf(false) }

    ResultCard(
        main = {
            SelectionContainer {
                Text(
                    outputs.getText(),
                    Modifier
                        .testTag("geoShareConversionSuccessPositionCoordinates")
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            outputs.getSupportingText().takeIf { it.isNotEmpty() }?.let {
                SelectionContainer {
                    Text(
                        it,
                        Modifier
                            .testTag("geoShareConversionSuccessPositionParams")
                            .fillMaxWidth(),
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        after = {
            IconButton({ setSheetVisible(true) }) {
                Icon(
                    painterResource(R.drawable.content_copy_24px),
                    contentDescription = stringResource(R.string.conversion_succeeded_copy_content_description)
                )
            }
        },
        bottom = outputs.getPointActions().takeIf { it.size > 1 }?.let { outputs ->
            {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                    outputs.groupBy { it.i }.map { (i, outputs) ->
                        ResultSuccessPoint(i, outputs, onRun)
                    }
                }
            }
        },
        chips = {
            outputs.getChips().forEach { (action, label) ->
                ResultCardChip(label()) {
                    onRun(action)
                }
            }
        },
    )
    ResultSuccessSheet(
        outputs = outputs.getActions(),
        sheetVisible = sheetVisible,
        onSetSheetVisible = setSheetVisible,
        onRun = onRun,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            @Suppress("SpellCheckingInspection")
            val packageNames = listOf(
                IntentTools.GOOGLE_MAPS_PACKAGE_NAME,
                MagicEarthOutputManager.PACKAGE_NAME,
                "app.comaps.fdroid",
                "app.organicmaps",
                "com.here.app.maps",
                "cz.seznam.mapy",
                "net.osmand.plus",
                "us.spotco.maps",
            )
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(Position.example, packageNames),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            @Suppress("SpellCheckingInspection")
            val packageNames = listOf(
                IntentTools.GOOGLE_MAPS_PACKAGE_NAME,
                MagicEarthOutputManager.PACKAGE_NAME,
                "app.comaps.fdroid",
                "app.organicmaps",
                "com.here.app.maps",
                "cz.seznam.mapy",
                "net.osmand.plus",
                "us.spotco.maps",
            )
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(Position.example, packageNames),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OneAppPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val packageNames = listOf(IntentTools.GOOGLE_MAPS_PACKAGE_NAME)
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(Position.example, packageNames),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkOneAppPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            val packageNames = listOf(IntentTools.GOOGLE_MAPS_PACKAGE_NAME)
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(Position.example, packageNames),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParamsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(
                    Position.example.copy(q = "Berlin, Germany", z = "13"),
                    emptyList()
                ),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkParamsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(
                    Position.example.copy(q = "Berlin, Germany", z = "13"),
                    emptyList()
                ),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PointsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(
                    Position(
                        points = persistentListOf(
                            Point.genRandomPoint(),
                            Point.genRandomPoint(),
                            Point.genRandomPoint(),
                        ),
                    ), emptyList()
                ),
                onRun = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPointsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            ResultSuccessCoordinates(
                outputs = allOutputManagers.getOutputs(
                    Position(
                        points = persistentListOf(
                            Point.genRandomPoint(),
                            Point.genRandomPoint(),
                            Point.genRandomPoint(),
                        ),
                    ), emptyList()
                ),
                onRun = {},
            )
        }
    }
}
