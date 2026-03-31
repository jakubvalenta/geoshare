package page.ooooo.geoshare.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun <T> AnimatedMessage(
    state: T,
    isMessageShown: (state: T?) -> Boolean,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    content: @Composable AnimatedContentScope.(T?) -> Unit,
) {
    var targetState by remember {
        mutableStateOf(
            if (animationsEnabled && isMessageShown(state)) {
                // To make the message appear with an animation, first start with null state and only later change it to
                // the current state (using LaunchedEffect).
                null
            } else {
                state
            }
        )
    }

    LaunchedEffect(state) {
        targetState = state
    }

    AnimatedContent(
        targetState,
        modifier = modifier,
        transitionSpec = {
            if (!animationsEnabled) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val initialStateMessageShown = isMessageShown(this.initialState)
                val targetStateMessageShown = isMessageShown(this.targetState)
                if (!initialStateMessageShown && !targetStateMessageShown) {
                    // Message stays hidden
                    EnterTransition.None togetherWith ExitTransition.None
                } else if (targetStateMessageShown) {
                    // Showing message or changing shown message
                    slideInHorizontally { fullWidth -> -fullWidth } togetherWith fadeOut()
                } else {
                    // Hiding message
                    fadeIn() togetherWith fadeOut()
                }
            }
        },
        content = content,
    )
}
