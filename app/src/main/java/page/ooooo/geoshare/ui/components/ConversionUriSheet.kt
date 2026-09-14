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
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.lib.android.AppActivity
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.TextActivity
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.copy
import page.ooooo.geoshare.lib.android.getPackageNames
import page.ooooo.geoshare.lib.android.queryActivitiesForUri
import page.ooooo.geoshare.lib.android.queryAppDetails
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionUriSheet(
    uriString: String,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    val activitiesForUri = remember(uriString) { context.packageManager.queryActivitiesForUri(uriString) }
    var appDetailsForUri by remember { mutableStateOf<AppDetails>(emptyMap()) }

    LaunchedEffect(activitiesForUri) {
        if (activitiesForUri.isNotEmpty()) {
            appDetailsForUri = context.packageManager.queryAppDetails(activitiesForUri.getPackageNames())
        }
    }

    ConversionUriSheet(
        uriString = uriString,
        activitiesForUri = activitiesForUri,
        appDetailsForUri = appDetailsForUri,
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversionUriSheet(
    uriString: String,
    activitiesForUri: List<AppActivity>,
    appDetailsForUri: AppDetails,
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
            activitiesForUri
                .takeIf { it.isNotEmpty() }
                ?.map { activity -> activity to appDetailsForUri[activity.packageName]?.label }
                ?.sortedWith(compareBy(nullsLast()) { (_, label) -> label })
                ?.let { activitiesAndLabels ->
                    SheetSection(
                        first = false,
                        title = stringResource(R.string.main_source_open),
                    ) {
                        activitiesAndLabels.forEach { (activity, label) ->
                            when (activity) {
                                is FileActivity -> {
                                    // Don't show an item for a file activity, because we don't know how to create a
                                    // file from the source, which is a URI or a text
                                }

                                is TextActivity ->
                                    ConversionUriSheetItem(activity, appDetailsForUri, label) {
                                        activity.launch(context, uriString)
                                        onDismissRequest()
                                    }

                                is UriActivity ->
                                    ConversionUriSheetItem(activity, appDetailsForUri, label) {
                                        activity.launch(context, uriString)
                                        onDismissRequest()
                                    }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun ConversionUriSheetItem(
    activity: AppActivity,
    appDetailsForUri: AppDetails,
    label: String?,
    onClick: () -> Unit,
) {
    SheetListItem(
        headlineText = label.orEmpty(),
        modifier = Modifier.testTag("geoShareConversionUriSheetItem_${activity.packageName}"),
        onClick = onClick,
        icon = appDetailsForUri[activity.packageName]?.icon?.let { DrawableIconDescriptor(it) }
            ?: PlaceholderIconDescriptor,
    )
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
                activitiesForUri = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.CONVERSATIONS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetailsForUri = fakeAppDetails(),
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
                activitiesForUri = fakeActivities.filter {
                    it.packageName in setOf(
                        PackageNames.COMAPS_FDROID,
                        PackageNames.CONVERSATIONS,
                        PackageNames.OSMAND_PLUS,
                    )
                },
                appDetailsForUri = fakeAppDetails(),
                initialValue = SheetValue.Expanded,
                onDismissRequest = {},
            )
        }
    }
}
