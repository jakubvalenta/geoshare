package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.LocalSpacing

private fun createLinearFunc(x1: Float, y1: Float, x2: Float, y2: Float): (x: Int) -> Float {
    val a = (y2 - y1) / (x2 - x1)
    val b = y1 - a * x1
    return fun(x: Int) = a * x + b
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollablePane(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    navigationImageVector: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val spacing = LocalSpacing.current

    val lazyListState = rememberLazyListState()
    val headlineHeightPx = with(density) { MaterialTheme.typography.headlineMedium.fontSize.toPx() }
    val headlinePaddingTopPx = with(density) { spacing.largeAdaptive.toPx() }
    val headlinePaddingBottomPx = with(density) { spacing.mediumAdaptive.toPx() }
    val calcHeadlineAlpha = createLinearFunc(
        headlinePaddingTopPx, 1f,
        headlinePaddingTopPx + headlineHeightPx, 0f,
    )
    val calcTitleAlpha = createLinearFunc(
        headlinePaddingTopPx + headlineHeightPx, 0f,
        headlinePaddingTopPx + headlineHeightPx + headlinePaddingBottomPx, 1f,
    )
    val headlineAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                calcHeadlineAlpha(lazyListState.firstVisibleItemScrollOffset).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }
    val titleAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                calcTitleAlpha(lazyListState.firstVisibleItemScrollOffset).coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    }

    TopAppBar(
        title = {
            Box(Modifier.graphicsLayer { alpha = titleAlpha }) {
                title()
            }
        },
        navigationIcon = {
            if (onBack != null) {
                FilledIconButton(
                    onBack,
                    Modifier.testTag("geoShareBack"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        imageVector = navigationImageVector,
                        contentDescription = stringResource(R.string.nav_back_content_description)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
    LazyColumn(modifier.fillMaxHeight(), lazyListState) {
        item {
            Box(
                Modifier
                    .graphicsLayer { alpha = headlineAlpha }
                    .padding(top = spacing.headlineTopAdaptive, bottom = spacing.mediumAdaptive),
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineMedium) {
                    title()
                }
            }
        }
        this.content()
    }
}
