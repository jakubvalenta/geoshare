package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainForm(
    source: String,
    errorMessageResId: Int?,
    inputRepository: InputRepository,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onSubmit: () -> Unit,
    onUpdateInput: (newSource: String) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val spacing = LocalSpacing.current

    val randomPointName = stringResource(R.string.intro_how_to_share_google_maps_screenshot_place)
    val randomPoint = remember { WGS84Point(NaivePoint.genRandomPoint(name = randomPointName)) }
    val randomLinks = remember {
        setOf(
            inputRepository.googleMapsUriInput,
            inputRepository.appleMapsUriInput,
            inputRepository.openStreetMapUriInput,
            *inputRepository.all.toTypedArray(),
        ).mapNotNull { (it as? Input.HasRandomUri)?.genRandomUri(randomPoint) }
    }

    LabelLarge(
        stringResource(R.string.main_input_uri_label),
        Modifier
            .padding(horizontal = spacing.windowPadding + 2.dp)
            .padding(top = spacing.small),
        color = MaterialTheme.colorScheme.primary,
    )
    AnimatedPlaceholderTextField(
        value = source,
        onValueChange = {
            onUpdateInput(it)
            onSetErrorMessageResId(null)
        },
        placeholders = randomLinks,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.windowPadding)
            .padding(top = spacing.small)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSubmit()
                    true
                } else {
                    false
                }
            }
            .testTag("geoShareMainSourceTextField"),
        leadingIcon = {
            if (source.isNotEmpty()) {
                IconButton(
                    {
                        onUpdateInput("")
                        onSetErrorMessageResId(null)
                    },
                    Modifier.offset(x = 4.dp),
                ) {
                    Icon(
                        Icons.Default.Clear,
                        stringResource(R.string.main_input_uri_clear_content_description),
                    )
                }
            } else {
                IconButton(
                    {
                        coroutineScope.launch {
                            onUpdateInput(AndroidTools.pasteFromClipboard(clipboard))
                            onSetErrorMessageResId(null)
                        }
                    },
                    Modifier.offset(x = 4.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.content_paste_24px),
                        stringResource(R.string.main_input_uri_paste_content_description),
                    )
                }
            }
        },
        trailingIcon = {
            FilledIconButton(
                onClick = {
                    if (source.isEmpty()) {
                        // To show the user immediate feedback on this screen, do a simple validation before
                        // starting the conversion. Else the user would see an error message only on the conversion
                        // screen.
                        onSetErrorMessageResId(R.string.conversion_failed_missing_url)
                    } else {
                        onSubmit()
                    }
                },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("geoShareMainSubmitButton"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = stringResource(R.string.main_create_geo_uri)
                )
            }
        },
        supportingText = {
            Text(
                stringResource(errorMessageResId ?: R.string.main_input_uri_supporting_text),
                Modifier.padding(horizontal = 4.dp),
            )
        },
        // TODO
        // supportingText = {
        //     ParagraphText(
        //         annotatedStringResource(
        //             R.string.main_text,
        //             FormatArg.Link(
        //                 stringResource(R.string.main_navigate_to_intro),
        //                 onNavigateToIntroScreen,
        //             ),
        //         ),
        //         color = MaterialTheme.colorScheme.onSurfaceVariant,
        //         style = MaterialTheme.typography.bodySmall,
        //     )
        // },
        isError = errorMessageResId != null,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() },
        ),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = contentPaddingWithoutLabel(start = 0.dp),
    )
    ScrollableChips(
        PaddingValues(
            start = spacing.windowPadding + 1.dp,
            end = spacing.windowPadding + 1.dp,
            top = spacing.small,
        )
    ) {
        item {
            FilledSuggestionChip(
                label = stringResource(R.string.main_random),
                icon = {
                    Icon(painterResource(R.drawable.ifl_24px), null)
                },
            ) {
                inputRepository
                    .all
                    .shuffled()
                    .firstNotNullOfOrNull { (it as? Input.HasRandomUri)?.genRandomUri(randomPoint) }
                    ?.let { newSource ->
                        onUpdateInput(newSource)
                        onSetErrorMessageResId(null)
                    }
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
                    inputRepository = FakeInputRepository,
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
                    inputRepository = FakeInputRepository,
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
                    inputRepository = FakeInputRepository,
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
                    inputRepository = FakeInputRepository,
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
                    inputRepository = FakeInputRepository,
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
                    inputRepository = FakeInputRepository,
                    onSetErrorMessageResId = {},
                    onUpdateInput = {},
                    onSubmit = {},
                )
            }
        }
    }
}
