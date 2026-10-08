package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.android.paste
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainTextField(
    errorMessageResId: Int?,
    source: StateFlow<String>,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSetSource: (newSource: String) -> Unit,
    onSubmit: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    val source by source.collectAsStateWithLifecycle()

    OutlinedTextField(
        value = source,
        onValueChange = {
            onSetSource(it)
            onSetErrorMessageResId(null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.windowPadding)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSubmit()
                    true
                } else {
                    false
                }
            }
            .testTag("geoShareMainSourceTextField"),
        label = {
            Text(stringResource(R.string.main_input_uri_label))
        },
        trailingIcon = {
            if (source.isNotEmpty()) {
                IconButton({
                    onSetSource("")
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
                        onSetSource(clipboard.paste())
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
                Modifier.padding(top = spacing.extraTiny),
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
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            MainTextField(
                errorMessageResId = null,
                source = MutableStateFlow(""),
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            MainTextField(
                errorMessageResId = null,
                source = MutableStateFlow(""),
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilledPreview() {
    AppTheme {
        Surface {
            MainTextField(
                errorMessageResId = null,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFilledPreview() {
    AppTheme {
        Surface {
            MainTextField(
                errorMessageResId = null,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        Surface {
            MainTextField(
                errorMessageResId = R.string.conversion_failed_missing_url,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        Surface {
            MainTextField(
                errorMessageResId = R.string.conversion_failed_missing_url,
                source = MutableStateFlow("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                onSetErrorMessageResId = {},
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}
