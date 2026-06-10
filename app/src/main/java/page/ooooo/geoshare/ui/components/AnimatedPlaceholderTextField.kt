package page.ooooo.geoshare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPaddingWithoutLabel
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import page.ooooo.geoshare.ui.theme.AppTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A text field whose placeholder cycles through [placeholders] every [placeholderCycleInterval].
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedPlaceholderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholders: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    leadingIcon: @Composable (() -> Unit)? = null,
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
    val textColor = textStyle.color.takeOrElse {
        val focused = interactionSource.collectIsFocusedAsState().value
        colors.textColor(enabled, isError, focused)
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    var currentPlaceholderIndex by remember { mutableIntStateOf(0) }
    val placeholderAlpha by animateFloatAsState(targetValue = if (isFocused) 0f else 1f, label = "placeholderAlpha")
    val placeholderColor = colors.placeholderColor(enabled, isError, isFocused)
    val placeholderVisible by remember { derivedStateOf(structuralEqualityPolicy()) { placeholderAlpha > 0f } }

    val animationDurationMs = placeholderAnimationDuration.inWholeMilliseconds.toInt()
    val enterTransition = slideInVertically(
        animationSpec = tween(animationDurationMs, easing = EaseOutCubic),
        initialOffsetY = { fullHeight -> fullHeight }
    ) + fadeIn(tween(animationDurationMs))
    val exitTransition = slideOutVertically(
        animationSpec = tween(animationDurationMs, easing = EaseInCubic),
        targetOffsetY = { fullHeight -> -fullHeight }
    ) + fadeOut(tween(animationDurationMs))

    LaunchedEffect(isFocused, value) {
        if (!isFocused && value.isEmpty() && placeholders.size > 1) {
            while (true) {
                delay(placeholderCycleInterval)
                currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholders.size
            }
        }
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = TextFieldDefaults.MinHeight,
        ),
        enabled = enabled,
        textStyle = mergedTextStyle,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = {
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (!isFocused && value.isEmpty() && placeholders.size > 1 && placeholderVisible) {
                            // Don't render placeholder when it's invisible for accessibility with screen readers
                            AnimatedContent(
                                targetState = currentPlaceholderIndex,
                                transitionSpec = { enterTransition togetherWith exitTransition },
                            ) { index ->
                                Text(
                                    placeholders[index],
                                    Modifier.graphicsLayer { alpha = placeholderAlpha },
                                    color = placeholderColor,
                                    softWrap = false,
                                    maxLines = 1,
                                    style = textStyle,
                                )
                            }
                        }
                        innerTextField()
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = supportingText,
                singleLine = true,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
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

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            var text by remember { mutableStateOf("") }
            AnimatedPlaceholderTextField(
                value = text,
                onValueChange = { text = it },
                placeholders = listOf(
                    "flights to Tokyo",
                    "hotels in Paris",
                    "things to do in New York",
                    "weekend getaways near me",
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
    }
}
