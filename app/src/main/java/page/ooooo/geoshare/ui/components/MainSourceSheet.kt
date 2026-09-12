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
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.openUriInDefaultApp
import page.ooooo.geoshare.lib.android.queryAppDetailsForUri
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSourceSheet(
    source: StateFlow<String>,
    initialAppDetails: AppDetails = emptyMap(),
    initialValue: SheetValue = SheetValue.Hidden,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val source by source.collectAsStateWithLifecycle()

    var appDetailsForUri by remember { mutableStateOf(initialAppDetails) }
    val sheetState = rememberBottomSheetState(initialValue)

    LaunchedEffect(source) {
        appDetailsForUri = context.packageManager.queryAppDetailsForUri(source)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        // TODO Add copy item
        appDetailsForUri.values.filterNotNull().forEach { appDetail ->
            // TODO Rename ResultSheetItem
            ResultSheetItem(
                headlineText = appDetail.label,
                onClick = { context.openUriInDefaultApp(source) },
                icon = DrawableIconDescriptor(appDetail.icon),
            )
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
            MainSourceSheet(
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                initialAppDetails = fakeAppDetails(),
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
            MainSourceSheet(
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                initialAppDetails = fakeAppDetails(),
                initialValue = SheetValue.Expanded,
                onDismissRequest = {},
            )
        }
    }
}
