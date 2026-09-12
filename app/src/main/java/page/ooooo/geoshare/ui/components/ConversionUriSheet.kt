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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.fakeApps
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.Apps
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.copy
import page.ooooo.geoshare.lib.android.openUriInApp
import page.ooooo.geoshare.lib.android.queryAppDetails
import page.ooooo.geoshare.lib.android.queryAppsForUri
import page.ooooo.geoshare.lib.android.sendTextViaApp
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionUriSheet(
    uriString: String,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    var appDetailsForUri by remember { mutableStateOf<AppDetails>(emptyMap()) }
    val appsForUri = remember(uriString) { context.packageManager.queryAppsForUri(uriString) }

    LaunchedEffect(appsForUri) {
        if (appsForUri.isNotEmpty()) {
            appDetailsForUri = context.packageManager.queryAppDetails(appsForUri.keys)
        }
    }

    ConversionUriSheet(
        uriString = uriString,
        appDetailsForUri = appDetailsForUri,
        appsForUri = appsForUri,
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversionUriSheet(
    uriString: String,
    appDetailsForUri: AppDetails,
    appsForUri: Apps,
    initialValue: SheetValue = SheetValue.Hidden,
    onDismissRequest: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
                OneLineSheetListItem(
                    headlineText = uriString,
                    onClick = {
                        coroutineScope.launch {
                            clipboard.copy(uriString)
                            onDismissRequest()
                        }
                    },
                    icon = ResourceIconDescriptor(R.drawable.content_copy_24px),
                )
            }
            appsForUri
                .takeIf { it.isNotEmpty() }
                ?.values
                ?.map { app -> app to appDetailsForUri[app.packageName]?.label }
                ?.sortedWith(compareBy(nullsLast()) { (_, label) -> label })
                ?.let { appsAndLabels ->
                    SheetSection(
                        first = false,
                        title = stringResource(R.string.main_source_open),
                    ) {
                        appsAndLabels.forEach { (app, label) ->
                            SheetListItem(
                                headlineText = label.orEmpty(),
                                onClick = {
                                    if (DataType.SEND_PLAIN_TEXT in app.dataTypes) {
                                        // TODO Add instrumented test
                                        context.sendTextViaApp(uriString, app.packageName)
                                    } else {
                                        context.openUriInApp(uriString, app.packageName)
                                    }
                                    onDismissRequest()
                                },
                                icon = appDetailsForUri[app.packageName]?.icon?.let { DrawableIconDescriptor(it) }
                                    ?: PlaceholderIconDescriptor,
                            )
                        }
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
            ConversionUriSheet(
                uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                appDetailsForUri = fakeAppDetails(),
                appsForUri = fakeApps.filterKeys {
                    it in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.CONVERSATIONS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                initialValue = SheetValue.Expanded,
                onDismissRequest = {},
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
            ConversionUriSheet(
                uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                appDetailsForUri = fakeAppDetails(),
                appsForUri = fakeApps.filterKeys {
                    it in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.CONVERSATIONS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                initialValue = SheetValue.Expanded,
                onDismissRequest = {},
            )
        }
    }
}
