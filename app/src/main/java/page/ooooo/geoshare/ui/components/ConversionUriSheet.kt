package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.CopyStringOutput
import page.ooooo.geoshare.ui.OutputDetailsForUriByCategory
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.toOutputDetailsForUriByCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionUriSheet(
    outputsForUriByCategory: StateFlow<OutputDetailsForUriByCategory>,
    uriString: String,
    initialValue: SheetValue = SheetValue.Hidden,
    onDismissRequest: () -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    val outputsForUriByCategory by outputsForUriByCategory.collectAsStateWithLifecycle()
    val sheetState = rememberBottomSheetState(initialValue)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("geoShareConversionUriSheet"),
        sheetState = sheetState,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            SheetSection(title = stringResource(R.string.conversion_succeeded_skip)) {
                outputsForUriByCategory.copy.forEach { outputDetail ->
                    OneLineSheetListItem(
                        headlineText = outputDetail.output.getDescription(uriString).orEmpty(),
                        onClick = {
                            onDismissRequest()
                            onExecute(outputDetail.output.toAction(uriString))
                        },
                        icon = ResourceIconDescriptor(R.drawable.content_copy_24px),
                    )
                }
            }
            // TODO Sort by label
            outputsForUriByCategory.open.forEach { outputDetail ->
                SheetSection(
                    first = false,
                    title = stringResource(R.string.main_source_open),
                ) {
                    SheetListItem(
                        headlineText = outputDetail.label(),
                        modifier = Modifier.testTag("geoShareConversionUriSheetItem_${outputDetail.output.id}"),
                        onClick = {
                            onDismissRequest()
                            onExecute(outputDetail.output.toAction(uriString))
                        },
                        icon = outputDetail.icon,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails: AppDetails = emptyMap()
            ConversionUriSheet(
                outputsForUriByCategory = MutableStateFlow(
                    outputRepository.getOutputsForUri(
                        fakeActivities.filter {
                            it.packageName in setOf(
                                PackageNames.COMAPS_FDROID,
                                PackageNames.CONVERSATIONS,
                                PackageNames.OSMAND_PLUS,
                            )
                        }
                    )
                        .partition { it is CopyStringOutput }
                        .toOutputDetailsForUriByCategory(appDetails)
                ),
                uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                initialValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val log = DefaultLog
            val outputRepository = OutputRepository(
                coordinateConverter = coordinateConverter,
                log = log,
            )
            val appDetails: AppDetails = emptyMap()
            ConversionUriSheet(
                outputsForUriByCategory = MutableStateFlow(
                    outputRepository.getOutputsForUri(
                        fakeActivities.filter {
                            it.packageName in setOf(
                                PackageNames.COMAPS_FDROID,
                                PackageNames.CONVERSATIONS,
                                PackageNames.OSMAND_PLUS,
                            )
                        }
                    )
                        .partition { it is CopyStringOutput }
                        .toOutputDetailsForUriByCategory(appDetails)
                ),
                uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                initialValue = SheetValue.Expanded,
                onDismissRequest = {},
                onExecute = {},
            )
        }
    }
}
