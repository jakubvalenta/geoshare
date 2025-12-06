package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun InputsText(onShowOpenByDefaultSettings: () -> Unit) {
    val appName = stringResource(R.string.app_name)
    val spacing = LocalSpacing.current
    Column(Modifier.padding(start = spacing.windowPadding, top = spacing.tiny, end = spacing.windowPadding)) {
        ParagraphHtml(stringResource(R.string.url_converters_text, appName))
        Button({ onShowOpenByDefaultSettings() }) {
            Text(stringResource(R.string.url_converters_settings_button))
        }
    }
}
