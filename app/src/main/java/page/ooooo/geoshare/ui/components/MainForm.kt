package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.paste
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainForm(
    placeholderUris: StateFlow<List<String>>,
    source: TextFieldState,
    initialError: String? = null,
    onSetSource: (newSource: String) -> Unit,
    onSubmit: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current
    val resources = LocalResources.current

    val empty by remember { derivedStateOf { source.text.isEmpty() } }
    var error by retain(initialError) { mutableStateOf(initialError) }
    val placeholderUris by placeholderUris.collectAsStateWithLifecycle()

    fun validateAndSubmit() {
        if (source.text.isEmpty()) {
            error = resources.getString(R.string.conversion_failed_missing_url)
        } else {
            error = null
            onSubmit()
        }
    }

    LaunchedEffect(source) {
        snapshotFlow { source.text.toString() }
            .drop(1) // Skip initial value
            .collectLatest { newSource ->
                error = null
                onSetSource(newSource)
            }
    }

    Column(Modifier.padding(horizontal = spacing.windowPadding)) {
        Text(
            stringResource(R.string.main_input_uri_label),
            Modifier.padding(start = 2.dp, bottom = spacing.tiny),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
        AnimatedPlaceholderTextField(
            state = source,
            placeholders = placeholderUris,
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        validateAndSubmit()
                        true
                    } else {
                        false
                    }
                }
                .testTag("geoShareMainSourceTextField"),
            trailingIcon = {
                if (!empty) {
                    IconButton({ onSetSource("") }) {
                        Icon(
                            Icons.Default.Clear,
                            stringResource(R.string.main_input_uri_clear_content_description),
                        )
                    }
                } else {
                    IconButton({
                        coroutineScope.launch {
                            onSetSource(clipboard.paste())
                        }
                    }) {
                        Icon(
                            painterResource(R.drawable.content_paste_24px),
                            stringResource(R.string.main_input_uri_paste_content_description),
                        )
                    }
                }
            },
            supportingText = error?.let { error ->
                {
                    Text(error, Modifier.padding(top = spacing.extraTiny))
                }
            },
            isError = error != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = { validateAndSubmit() },
        )
        LargeButton(
            stringResource(R.string.main_create_geo_uri),
            Modifier
                .padding(top = spacing.small)
                .testTag("geoShareMainSubmitButton"),
            onClick = ::validateAndSubmit,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            MainForm(
                placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                source = TextFieldState(),
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
            MainForm(
                placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                source = TextFieldState(),
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
            MainForm(
                placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                source = TextFieldState("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
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
            MainForm(
                placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                source = TextFieldState("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
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
            MainForm(
                placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                source = TextFieldState("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                initialError = stringResource(R.string.conversion_failed_missing_url),
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
            MainForm(
                placeholderUris = MutableStateFlow(listOf("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8")),
                source = TextFieldState("https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"),
                initialError = stringResource(R.string.conversion_failed_missing_url),
                onSetSource = {},
                onSubmit = {},
            )
        }
    }
}
