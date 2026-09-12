package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.copy
import page.ooooo.geoshare.lib.android.openUriInApp
import page.ooooo.geoshare.lib.android.queryAppDetailsForUri
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionUriSheet(
    uriString: String,
    initialAppDetails: AppDetails = emptyMap(),
    initialValue: SheetValue = SheetValue.Hidden,
    preview: Boolean = false,
    onDismissRequest: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var appDetailsForUri by remember { mutableStateOf(initialAppDetails) }
    val sheetState = rememberBottomSheetState(initialValue)

    LaunchedEffect(uriString) {
        if (!preview) {
            appDetailsForUri = context.packageManager.queryAppDetailsForUri(uriString)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        SheetSection {
            SheetListItem(
                headlineText = stringResource(R.string.conversion_succeeded_skip),
                onClick = {
                    coroutineScope.launch {
                        clipboard.copy(uriString)
                        onDismissRequest()
                    }
                },
                supportingText = uriString,
                icon = ResourceIconDescriptor(R.drawable.content_copy_24px),
            )
        }
        appDetailsForUri.values
            .filterNotNull()
            .takeIf { it.isNotEmpty() }
            ?.let { appDetails ->
                SheetSection(
                    first = false,
                    title = stringResource(R.string.main_source_open),
                ) {
                    appDetails.forEach { appDetail ->
                        SheetListItem(
                            headlineText = appDetail.label,
                            onClick = {
                                context.openUriInApp(uriString, appDetail.packageName)
                                onDismissRequest()
                            },
                            icon = DrawableIconDescriptor(appDetail.icon),
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
            ConversionUriSheet(
                uriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                initialAppDetails = fakeAppDetails(),
                initialValue = SheetValue.Expanded,
                preview = true,
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
                initialAppDetails = fakeAppDetails(),
                initialValue = SheetValue.Expanded,
                preview = true,
                onDismissRequest = {},
            )
        }
    }
}
