package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldPredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T> NavigableBasicListDetailScaffold(
    navigator: ThreePaneScaffoldNavigator<T>,
    listPane: @Composable ColumnScope.(wide: Boolean, containerColor: Color) -> Unit,
    detailPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    listContainerColor: Color = Color.Transparent,
    listContentColor: Color = contentColor,
    defaultBackBehavior: BackNavigationBehavior =
        BackNavigationBehavior.PopUntilScaffoldValueChange,
) {
    ThreePaneScaffoldPredictiveBackHandler(
        navigator = navigator,
        backBehavior = defaultBackBehavior,
    )

    BasicListDetailScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        listPane = listPane,
        detailPane = detailPane,
        containerColor = containerColor,
        contentColor = contentColor,
        listContainerColor = listContainerColor,
        listContentColor = listContentColor,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BasicListDetailScaffold(
    directive: PaneScaffoldDirective,
    scaffoldState: ThreePaneScaffoldState,
    listPane: @Composable ColumnScope.(wide: Boolean, containerColor: Color) -> Unit,
    detailPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    listContainerColor: Color = Color.Transparent,
    listContentColor: Color = contentColor,
) {
    val layoutDirection = LocalLayoutDirection.current

    val wide = remember(scaffoldState) {
        scaffoldState.targetState.primary == PaneAdaptedValue.Expanded &&
            scaffoldState.targetState.secondary == PaneAdaptedValue.Expanded
    }
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()

    ListDetailPaneScaffold(
        directive = directive,
        scaffoldState = scaffoldState,
        listPane = {
            AnimatedPane {
                val containerPadding = if (wide) {
                    PaddingValues(
                        start = insetPadding.calculateStartPadding(layoutDirection),
                        top = insetPadding.calculateTopPadding(),
                        bottom = insetPadding.calculateBottomPadding(),
                    )
                } else {
                    insetPadding
                }
                val listContainerColor = if (wide) listContainerColor else containerColor
                val listContentColor = if (wide) listContentColor else contentColor
                Column(
                    Modifier
                        .background(listContainerColor)
                        .padding(containerPadding)
                        .consumeWindowInsets(containerPadding)
                ) {
                    CompositionLocalProvider(LocalContentColor provides listContentColor) {
                        listPane(wide, listContainerColor)
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val containerPadding = if (wide) {
                    PaddingValues(
                        top = insetPadding.calculateTopPadding(),
                        end = insetPadding.calculateEndPadding(layoutDirection),
                        bottom = insetPadding.calculateBottomPadding(),
                    )
                } else {
                    insetPadding
                }
                Column(
                    Modifier
                        .padding(containerPadding)
                        .consumeWindowInsets(containerPadding),
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        detailPane(wide)
                    }
                }
            }
        },
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .background(containerColor),
    )
}
