package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPaneScaffold(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    firstPane: (@Composable ColumnScope.() -> Unit)? = null,
    secondPane: (@Composable ColumnScope.() -> Unit)? = null,
    bottomPane: (@Composable ColumnScope.() -> Unit)? = null,
    actionsPane: (@Composable () -> Unit)? = null,
    dialog: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    expandedContainerColor: Color = containerColor,
    expandedContentColor: Color = contentColor,
    ratio: Float = 0.5f,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalSpacing.current
    val expanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (expanded) expandedContainerColor else containerColor,
                    navigationIconContentColor = if (expanded) expandedContentColor else contentColor,
                    actionIconContentColor = if (expanded) expandedContentColor else contentColor,
                ),
            )
        },
        containerColor = if (expanded) expandedContainerColor else containerColor,
        contentColor = if (expanded) expandedContainerColor else contentColor,
    ) { innerPadding ->
        val innerPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            top = innerPadding.calculateTopPadding(),
            end = innerPadding.calculateEndPadding(layoutDirection),
        )
        if (expanded) {
            Row(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
            ) {
                Column(Modifier.weight(ratio)) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (firstPane != null) {
                            Card(
                                Modifier.padding(horizontal = spacing.windowPadding),
                                colors = CardDefaults.cardColors(
                                    containerColor = containerColor,
                                    contentColor = contentColor,
                                ),
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = spacing.tiny,
                                            end = spacing.tiny,
                                            bottom = spacing.tinyAdaptive,
                                        ),
                                ) {
                                    firstPane()
                                }
                            }
                        }
                    }
                    if (bottomPane != null) {
                        bottomPane()
                    }
                }
                Column(Modifier.weight(1 - ratio)) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (secondPane != null) {
                            secondPane()
                        }
                        if (actionsPane != null) {
                            ElevatedCard(
                                Modifier.padding(end = spacing.windowPadding),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                actionsPane()
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f, true)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (firstPane != null) {
                        Card(
                            shape = RectangleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = containerColor,
                                contentColor = contentColor,
                            ),
                        ) {
                            firstPane()
                        }
                    }
                    if (secondPane != null) {
                        Card(
                            Modifier.weight(1f).fillMaxWidth(),
                            shape = RectangleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = expandedContainerColor,
                                contentColor = expandedContentColor,
                            ),
                        ) {
                            secondPane()
                        }
                    }
                }
                if (bottomPane != null) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = expandedContainerColor,
                            contentColor = expandedContentColor,
                        ),
                    ) {
                        bottomPane()
                    }
                }
                if (actionsPane != null) {
                    ElevatedCard(
                        shape = MaterialTheme.shapes.large.copy(
                            bottomStart = ZeroCornerSize,
                            bottomEnd = ZeroCornerSize,
                        ),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 20.dp),
                    ) {
                        Column(Modifier.safeDrawingPadding()) {
                            actionsPane()
                        }
                    }
                }
            }
        }
        if (dialog != null) {
            dialog()
        }
    }
}
