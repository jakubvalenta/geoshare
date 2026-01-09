package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.window.core.layout.WindowSizeClass
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoPaneScaffold(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    firstPane: (@Composable ColumnScope.() -> Unit)? = null,
    secondPane: (@Composable ColumnScope.() -> Unit)? = null,
    bottomPane: (@Composable ColumnScope.() -> Unit)? = null,
    dialog: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = Color.Unspecified,
    ratio: Float = 0.5f,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val spacing = LocalSpacing.current
    val expanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
            )
        },
    ) { innerPadding ->
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
                                        .padding(start = spacing.tiny, end = spacing.tiny, bottom = spacing.tinyAdaptive),
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
                    if (secondPane != null) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, true)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            secondPane()
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
                        secondPane()
                    }
                }
                if (bottomPane != null) {
                    bottomPane()
                }
            }
        }
        if (dialog != null) {
            dialog()
        }
    }
}
