package page.ooooo.geoshare.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIndicatorDialog(titleResId: Int, onDismissRequest: () -> Unit) {
    BasicAlertDialog(onDismissRequest) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(titleResId),
                    Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    color = AlertDialogDefaults.titleContentColor,
                    style = MaterialTheme.typography.headlineSmall,
                )
                LoadingIndicator(Modifier.size(120.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button({ onDismissRequest() }, Modifier.padding(top = 16.dp)) {
                    Text(stringResource(R.string.conversion_loading_indicator_cancel))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        LoadingIndicatorDialog(
            titleResId = R.string.converter_google_maps_loading_indicator_title,
            onDismissRequest = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        LoadingIndicatorDialog(
            titleResId = R.string.converter_google_maps_loading_indicator_title,
            onDismissRequest = {},
        )
    }
}
