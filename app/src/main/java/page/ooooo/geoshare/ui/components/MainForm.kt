package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainForm(
    source: String,
    errorMessageResId: Int?,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSubmit: () -> Unit,
    onUpdateInput: (newSource: String) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    Column(
        Modifier.padding(horizontal = spacing.windowPadding),
        verticalArrangement = Arrangement.spacedBy(spacing.mediumAdaptive),
    ) {
        OutlinedTextField(
            value = source,
            onValueChange = {
                onUpdateInput(it)
                onSetErrorMessageResId(null)
            },
            modifier = Modifier
                .testTag("geoShareMainSourceTextField")
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
                if (source.isNotEmpty()) {
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
                    Modifier.padding(top = spacing.tinyAdaptive),
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
        LargeButton(
            stringResource(R.string.main_create_geo_uri),
            Modifier.testTag("geoShareMainSubmitButton"),
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
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                MainForm(
                    source = "",
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
                    source = "",
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
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
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
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
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
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
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
                    source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA",
                    errorMessageResId = R.string.conversion_failed_missing_url,
                    onSetErrorMessageResId = {},
                    onUpdateInput = {},
                    onSubmit = {},
                )
            }
        }
    }
}
