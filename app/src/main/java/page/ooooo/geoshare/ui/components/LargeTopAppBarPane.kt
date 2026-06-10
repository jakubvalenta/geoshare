package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.LocalSpacing

/**
 * Renders a [TopAppBar] and a scrollable column with [title] and [content]. By default, there is no title in the top
 * bar. Only when the column is scrolled such that the title leaves the view box, then the title appears in the top bar.
 *
 * This is similar to [androidx.compose.material3.LargeTopAppBar] with nested scroll as described at
 * https://developer.android.com/develop/ui/compose/components/app-bars#large. However, the official version renders
 * a column that is scrollable even when there is not enough content for it to make sense, which our version doesn't
 * suffer from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopAppBarPane(
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    collapsedHeight: Dp = TopAppBarDefaults.LargeAppBarCollapsedHeight,
    expandedHeight: Dp = TopAppBarDefaults.LargeAppBarExpandedHeight,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    titleBottomPadding: Dp = 20.dp,
    navigationImageVector: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    content: LazyListScope.() -> Unit,
) {
    val appBarContainerColor = Color.Transparent
    val appBarContentColor = LocalContentColor.current
    val density = LocalDensity.current
    val spacing = LocalSpacing.current

    val listState = rememberLazyListState()
    // FIXME Not calculated correctly on result screen
    val titleHeightPx = with(density) { titleTextStyle.lineHeight.toPx() }
    val headlineTopOffsetPx = with(density) {
        expandedHeight.toPx() - collapsedHeight.toPx() - titleHeightPx - titleBottomPadding.toPx()
    }
    val titleAlphaSlope = 1 / titleHeightPx
    var titleAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .map { firstVisibleItemScrollOffset ->
                if (listState.firstVisibleItemIndex == 0) {
                    ((firstVisibleItemScrollOffset - headlineTopOffsetPx) * titleAlphaSlope).coerceIn(0f, 1f)
                } else {
                    1f
                }
            }
            .distinctUntilChanged()
            .collect { titleAlpha = it }
    }

    TopAppBar(
        title = {
            if (title != null) {
                Box(Modifier.graphicsLayer { alpha = titleAlpha }) {
                    title()
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onBack,
                    Modifier.testTag("geoShareBack"),
                ) {
                    Icon(
                        imageVector = navigationImageVector,
                        contentDescription = stringResource(R.string.nav_back_content_description),
                    )
                }
            }
        },
        actions = actions,
        expandedHeight = collapsedHeight,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appBarContainerColor,
            scrolledContainerColor = appBarContainerColor,
            navigationIconContentColor = appBarContentColor,
            titleContentColor = appBarContentColor,
            actionIconContentColor = appBarContentColor,
            subtitleContentColor = appBarContentColor,
        ),
    )
    LazyColumn(modifier, state = listState) {
        if (title != null) {
            item {
                Box(
                    Modifier
                        .height(expandedHeight - collapsedHeight)
                        .padding(horizontal = spacing.windowPadding)
                        .padding(bottom = titleBottomPadding)
                        .graphicsLayer { alpha = 1 - titleAlpha },
                    contentAlignment = Alignment.BottomStart,
                ) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineMedium) {
                        title()
                    }
                }
            }
        }
        content()
    }
}
