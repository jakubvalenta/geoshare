package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

/**
 * Renders a [TopAppBar] and a scrollable column with [title] and [content]. By default, there is no title in the top
 * bar. Only when the column is scrolled such that the title leaves the view box, then the title appears in the top bar.
 *
 * This is similar to [androidx.compose.material3.LargeTopAppBar] with nested scroll as described in
 * https://developer.android.com/develop/ui/compose/components/app-bars#large. However, the official version renders
 * a column that is scrollable even when the content is smaller than the view box, which our version doesn't suffer
 * from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopAppBarPane(
    modifier: Modifier = Modifier,
    title: (@Composable (maxLines: Int) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backIcon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    collapsedHeight: Dp = TopAppBarDefaults.LargeAppBarCollapsedHeight,
    expandedHeight: Dp = TopAppBarDefaults.LargeAppBarExpandedHeight,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = LocalContentColor.current,
        titleContentColor = LocalContentColor.current,
        actionIconContentColor = LocalContentColor.current,
        subtitleContentColor = LocalContentColor.current,
    ),
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    titleBottomPadding: Dp = 20.dp, // This seems to be the padding size that the default LargeTopAppBar uses
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val spacing = LocalSpacing.current

    val listState = rememberLazyListState()
    var titleAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState, title, collapsedHeight, expandedHeight) {
        if (title != null) {
            // Notice that we assume the title has only one line, while in reality it often has two and sometimes even
            // three lines. It means that when scrolling the pane, an expanded multi-line title disappears a bit earlier
            // than it should. But it's not a big deal, and it saves us some measuring of composables.
            val titleLineHeightPx = with(density) { titleTextStyle.lineHeight.toPx() }
            val headlineTopOffsetPx = with(density) {
                expandedHeight.toPx() - collapsedHeight.toPx() - titleLineHeightPx - titleBottomPadding.toPx()
            }
            val titleAlphaSlope = 1 / titleLineHeightPx
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
    }

    TopAppBar(
        title = {
            if (title != null) {
                Box(Modifier.graphicsLayer { alpha = titleAlpha }) {
                    title(1)
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
                        imageVector = backIcon,
                        contentDescription = stringResource(R.string.nav_back_content_description),
                    )
                }
            }
        },
        actions = actions,
        expandedHeight = collapsedHeight,
        colors = colors,
    )
    LazyColumn(modifier, state = listState) {
        if (title != null) {
            item {
                Column(
                    Modifier
                        .padding(horizontal = spacing.windowPadding)
                        .padding(bottom = titleBottomPadding)
                        .heightIn(min = expandedHeight - collapsedHeight)
                        .graphicsLayer { alpha = 1 - titleAlpha },
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineMedium) {
                        title(Int.MAX_VALUE)
                    }
                }
            }
        }
        content()
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .padding(innerPadding)
            ) {
                LargeTopAppBarPane(
                    title = { maxLines ->
                        Text(
                            @Suppress("SpellCheckingInspection") "Wikimedia Foundation, Inc., 1 Sansome St #1895, San Francisco, CA 94104, Vereinigte Staaten",
                            overflow = TextOverflow.Ellipsis,
                            maxLines = maxLines
                        )
                    },
                    onBack = {},
                    actions = {
                        IconButton({}) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.nav_menu_content_description),
                            )
                        }
                    },
                    content = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .padding(innerPadding)
            ) {
                LargeTopAppBarPane(
                    title = { maxLines ->
                        Text(
                            @Suppress("SpellCheckingInspection") "Wikimedia Foundation, Inc., 1 Sansome St #1895, San Francisco, CA 94104, Vereinigte Staaten",
                            overflow = TextOverflow.Ellipsis,
                            maxLines = maxLines,
                        )
                    },
                    onBack = {},
                    actions = {
                        IconButton({}) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.nav_menu_content_description),
                            )
                        }
                    },
                    content = {},
                )
            }
        }
    }
}
