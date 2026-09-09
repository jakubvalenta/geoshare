package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainSubmit(
    source: StateFlow<String>,
    modifier: Modifier = Modifier,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSubmit: () -> Unit,
) {
    val spacing = LocalSpacing.current

    val source by source.collectAsStateWithLifecycle()

    LargeButton(
        stringResource(R.string.main_create_geo_uri),
        modifier
            .padding(horizontal = spacing.windowPadding)
            .padding(top = spacing.small)
            .testTag("geoShareMainSubmitButton"),
    ) {
        if (source.isEmpty()) {
            // To show the user immediate feedback on this screen, do a simple validation before
            // starting the conversion. Else the user would see an error message only on the conversion
            // screen.
            onSetErrorMessageResId(R.string.conversion_failed_missing_url)
        } else {
            onSubmit()
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        MainSubmit(
            source = MutableStateFlow(""),
            onSetErrorMessageResId = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        MainSubmit(
            source = MutableStateFlow(""),
            onSetErrorMessageResId = {},
            onSubmit = {},
        )
    }
}
