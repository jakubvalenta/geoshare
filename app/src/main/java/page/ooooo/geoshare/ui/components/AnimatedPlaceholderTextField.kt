package page.ooooo.geoshare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.ui.theme.AppTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * A text field whose placeholder cycles through [placeholders] every [placeholderCycleInterval].
 */
@Composable
fun AnimatedPlaceholderTextField(
    @Suppress("SameParameterValue") value: String,
    onValueChange: (String) -> Unit,
    placeholders: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholderAnimationDuration: Duration = 1.seconds,
    placeholderCycleInterval: Duration = 4.seconds,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    contentPadding: PaddingValues = contentPaddingWithoutLabel(),
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val mergedTextStyle = remember(enabled, isError, isFocused) {
        textStyle.merge(color = colors.textColor(enabled, isError, isFocused))
    }

    var currentPlaceholderIndex by remember { mutableIntStateOf(0) }
    val placeholderAlpha by animateFloatAsState(targetValue = if (isFocused) 0f else 1f, label = "placeholderAlpha")
    val placeholderColor = remember(enabled, isError, isFocused) {
        colors.placeholderColor(enabled, isError, isFocused)
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

    LaunchedEffect(isFocused, value, placeholders) {
        if (!isFocused && value.isEmpty() && placeholders.isNotEmpty()) {
            while (true) {
                delay(placeholderCycleInterval)
                currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholders.size
            }
        }
    }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = TextFieldDefaults.MinHeight,
                )
                .clipToBounds(),
            enabled = enabled,
            textStyle = mergedTextStyle,
            singleLine = false,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            if (!isFocused && value.isEmpty() && placeholders.isNotEmpty()) {
                                AnimatedContent(
                                    targetState = currentPlaceholderIndex,
                                    transitionSpec = { placeholderTransition.using(SizeTransform(clip = false)) },
                                ) { index ->
                                    placeholders.getOrNull(index)?.let { placeholder ->
                                        Text(
                                            placeholder,
                                            Modifier.graphicsLayer { alpha = placeholderAlpha },
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
                    },
                    enabled = enabled,
                    singleLine = false,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    trailingIcon = trailingIcon,
                    supportingText = supportingText,
                    colors = colors,
                    contentPadding = contentPadding,
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
                value = "",
                onValueChange = {},
                placeholders = listOf(
                    "https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8",
                    "https://www.google.com/maps/search/?api=1&query=50.123456%2C-120.123456",
                    "https://maps.apple.com/?ll=50.123456%2C-120.123456&z=17&q=Dropped%20pin",
                    "geo:50.123456,-120.123456?q=50.123456,-120.123456(Dropped%20pin)",
                ),
                modifier = Modifier.padding(15.dp),
            )
        }
    }
}
