package page.ooooo.geoshare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * A text field whose placeholder cycles through [placeholders] every [placeholderCycleInterval].
 */
@Composable
fun AnimatedPlaceholderTextField(
    @Suppress("SameParameterValue") state: TextFieldState,
    placeholders: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    placeholderAnimationDuration: Duration = 1.seconds,
    placeholderCycleInterval: Duration = 4.seconds,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    contentPadding: PaddingValues = contentPaddingWithoutLabel(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled, isError, focused)
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    var currentPlaceholderIndex by remember { mutableIntStateOf(0) }
    val empty by remember { derivedStateOf { state.text.isEmpty() } }
    val placeholderColor = remember(enabled, isError, focused) {
        colors.placeholderColor(enabled, isError, focused)
    }

    val animationDurationMs = placeholderAnimationDuration.toInt(DurationUnit.MILLISECONDS)
    val placeholderTransition = remember(animationDurationMs) {
        slideInVertically(
            animationSpec = tween(animationDurationMs, easing = LinearEasing),
            initialOffsetY = { fullHeight -> 2 * fullHeight }
        ) togetherWith slideOutVertically(
            animationSpec = tween(animationDurationMs, easing = LinearEasing),
            targetOffsetY = { fullHeight -> -2 * fullHeight }
        )
    }

    LaunchedEffect(focused, empty, placeholders) {
        if (!focused && empty && placeholders.isNotEmpty()) {
            while (true) {
                delay(placeholderCycleInterval)
                currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholders.size
            }
        }
    }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        val decorator = OutlinedTextFieldDefaults.decorator(
            state = state,
            enabled = enabled,
            lineLimits = lineLimits,
            outputTransformation = null,
            interactionSource = interactionSource,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            isError = isError,
            colors = colors,
            contentPadding = PaddingValues.Zero,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = shape,
                )
            },
        )
        BasicTextField(
            state = state,
            modifier = modifier.defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight,
            ),
            enabled = enabled,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            interactionSource = interactionSource,
            decorator = TextFieldDecorator { innerTextField ->
                decorator.Decoration {
                    val layoutDirection = LocalLayoutDirection.current
                    Box(
                        Modifier
                            .clipToBounds()
                            .fillMaxWidth()
                            .padding(
                                start = contentPadding.calculateStartPadding(layoutDirection),
                                top = contentPadding.calculateTopPadding(),
                                end = if (trailingIcon == null) contentPadding.calculateEndPadding(layoutDirection) else 0.dp,
                                bottom = contentPadding.calculateBottomPadding()
                            ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (!focused && empty && placeholders.isNotEmpty()) {
                            AnimatedContent(
                                targetState = currentPlaceholderIndex,
                                transitionSpec = { placeholderTransition.using(SizeTransform(clip = false)) },
                            ) { index ->
                                placeholders.getOrNull(index)?.let { placeholder ->
                                    Text(
                                        placeholder,
                                        color = placeholderColor,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        maxLines = 1,
                                        style = mergedTextStyle,
                                    )
                                }
                            }
                        }
                        innerTextField()
                    }
                }
            },
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            AnimatedPlaceholderTextField(
                state = TextFieldState(),
                placeholders = listOf(
                    "https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8",
                    "https://www.google.com/maps/search/?api=1&query=50.123456%2C-120.123456",
                    "https://maps.apple.com/?ll=50.123456%2C-120.123456&z=17&q=Dropped%20pin",
                    "geo:50.123456,-120.123456?q=50.123456,-120.123456(Dropped%20pin)",
                ),
                modifier = Modifier.padding(15.dp),
                supportingText = { Text("My supporting text") },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilledPreview() {
    AppTheme {
        Surface {
            AnimatedPlaceholderTextField(
                state = TextFieldState("https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8"),
                placeholders = listOf(
                    "https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8",
                    "https://www.google.com/maps/search/?api=1&query=50.123456%2C-120.123456",
                    "https://maps.apple.com/?ll=50.123456%2C-120.123456&z=17&q=Dropped%20pin",
                    "geo:50.123456,-120.123456?q=50.123456,-120.123456(Dropped%20pin)",
                ),
                modifier = Modifier.padding(15.dp),
                trailingIcon = {
                    IconButton({}) {
                        Icon(
                            painterResource(R.drawable.content_paste_24px),
                            stringResource(R.string.main_input_uri_paste_content_description),
                        )
                    }
                },
                supportingText = { Text("My supporting text") },
            )
        }
    }
}
