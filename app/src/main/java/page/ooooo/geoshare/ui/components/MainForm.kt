package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainForm(
    inputUriString: String,
    errorMessageResId: Int?,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSubmit: () -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val spacing = LocalSpacing.current
    val appName = stringResource(R.string.app_name)

    Row(
        Modifier
            .padding(vertical = spacing.large)
            .padding(start = 13.dp, end = spacing.windowPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(R.string.about_app_icon_content_description),
            modifier = Modifier.size(
                with(density) { MaterialTheme.typography.headlineLarge.fontSize.toDp() * 2f }
            ),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(appName, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
    }
    Column(
        Modifier.padding(horizontal = spacing.windowPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        OutlinedTextField(
            value = inputUriString,
            onValueChange = {
                onUpdateInput(it)
                onSetErrorMessageResId(null)
            },
            modifier = Modifier
                .testTag("geoShareMainInputUriStringTextField")
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        onSubmit()
                        true
                    } else {
                        false
                    }
                }
                .fillMaxWidth(),
            label = {
                Text(stringResource(R.string.main_input_uri_label))
            },
            trailingIcon = {
                if (inputUriString.isNotEmpty()) {
                    IconButton({
                        onUpdateInput("")
                        onSetErrorMessageResId(null)
                    }) {
                        Icon(
                            Icons.Default.Clear,
                            stringResource(R.string.main_input_uri_clear_content_description),
                        )
                    }
                } else {
                    IconButton({
                        coroutineScope.launch {
                            onUpdateInput(AndroidTools.pasteFromClipboard(clipboard))
                            onSetErrorMessageResId(null)
                        }
                    }) {
                        Icon(
                            painterResource(R.drawable.content_paste_24px),
                            stringResource(R.string.main_input_uri_paste_content_description),
                        )
                    }
                }
            },
            supportingText = {
                Text(
                    stringResource(errorMessageResId ?: R.string.main_input_uri_supporting_text),
                    Modifier.padding(top = spacing.tiny),
                )
            },
            isError = errorMessageResId != null,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() },
            ),
        )
        Column(Modifier.padding(horizontal = 9.dp)) {
            Button(
                {
                    if (inputUriString.isEmpty()) {
                        // To show the user immediate feedback on this screen, do a simple validation before
                        // starting the conversion. Else the user would see an error message only on the conversion
                        // screen.
                        onSetErrorMessageResId(R.string.conversion_failed_missing_url)
                    } else {
                        onSubmit()
                    }
                },
                Modifier
                    .testTag("geoShareMainSubmitButton")
                    .align(Alignment.CenterHorizontally)
                    .width(400.dp)
            ) {
                Text(
                    stringResource(R.string.main_create_geo_uri),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5,
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                MainForm(
                    inputUriString = "",
                    errorMessageResId = null,
                    onSetErrorMessageResId = {},
                    onSubmit = {},
                    onUpdateInput = {},
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
                MainForm(
                    inputUriString = "",
                    errorMessageResId = null,
                    onSetErrorMessageResId = {},
                    onSubmit = {},
                    onUpdateInput = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilledPreview() {
    AppTheme {
        Surface {
            Column {
                MainForm(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    errorMessageResId = null,
                    onSetErrorMessageResId = {},
                    onSubmit = {},
                    onUpdateInput = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFilledPreview() {
    AppTheme {
        Surface {
            Column {
                MainForm(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    errorMessageResId = null,
                    onSetErrorMessageResId = {},
                    onSubmit = {},
                    onUpdateInput = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface {
            Column {
                MainForm(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    errorMessageResId = R.string.conversion_failed_missing_url,
                    onSetErrorMessageResId = {},
                    onUpdateInput = {},
                    onSubmit = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface {
            Column {
                MainForm(
                    inputUriString = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    errorMessageResId = R.string.conversion_failed_missing_url,
                    onSetErrorMessageResId = {},
                    onUpdateInput = {},
                    onSubmit = {},
                )
            }
        }
    }
}
