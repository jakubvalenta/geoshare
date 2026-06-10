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
import androidx.compose.foundation.layout.minus
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldState
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldPredictiveBackHandler
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Immutable
data class StyledPaneScaffoldColors(
    val containerColor: Color,
    val contentColor: Color,
    val mainContainerColor: Color,
    val mainContentColor: Color,
    val wideMainContainerColor: Color,
    val wideMainContentColor: Color,
)

object StyledPaneScaffoldDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        mainContainerColor: Color = Color.Transparent,
        mainContentColor: Color = contentColor,
        wideMainContainerColor: Color = mainContainerColor,
        wideMainContentColor: Color = mainContentColor,
    ) = StyledPaneScaffoldColors(
        containerColor = containerColor,
        contentColor = contentColor,
        mainContainerColor = mainContainerColor,
        mainContentColor = mainContentColor,
        wideMainContainerColor = wideMainContainerColor,
        wideMainContentColor = wideMainContentColor,
    )
}

/**
 * Allows setting [shouldAutoFocusCurrentDestination]
 */
private fun PaneScaffoldDirective.copy(
    maxHorizontalPartitions: Int = this.maxHorizontalPartitions,
    horizontalPartitionSpacerSize: Dp = this.horizontalPartitionSpacerSize,
    @Suppress("SameParameterValue") maxVerticalPartitions: Int = this.maxVerticalPartitions,
    verticalPartitionSpacerSize: Dp = this.verticalPartitionSpacerSize,
    defaultPanePreferredWidth: Dp = this.defaultPanePreferredWidth,
    excludedBounds: List<Rect> = this.excludedBounds,
    defaultPanePreferredHeight: Dp = this.defaultPanePreferredHeight,
    shouldAutoFocusCurrentDestination: Boolean = this.shouldAutoFocusCurrentDestination,
): PaneScaffoldDirective = PaneScaffoldDirective(
    maxHorizontalPartitions = maxHorizontalPartitions,
    horizontalPartitionSpacerSize = horizontalPartitionSpacerSize,
    maxVerticalPartitions = maxVerticalPartitions,
    verticalPartitionSpacerSize = verticalPartitionSpacerSize,
    defaultPanePreferredWidth = defaultPanePreferredWidth,
    defaultPanePreferredHeight = defaultPanePreferredHeight,
    excludedBounds = excludedBounds,
    shouldAutoFocusCurrentDestination = shouldAutoFocusCurrentDestination,
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StyledListDetailPaneScaffold(
    directive: PaneScaffoldDirective,
    scaffoldState: ThreePaneScaffoldState,
    listPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    detailPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    colors: StyledPaneScaffoldColors = StyledPaneScaffoldDefaults.colors(),
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
                Column(
                    Modifier
                        .background(if (wide) colors.wideMainContainerColor else colors.mainContainerColor)
                        .padding(containerPadding)
                        .consumeWindowInsets(insetPadding)
                ) {
                    CompositionLocalProvider(LocalContentColor provides if (wide) colors.wideMainContentColor else colors.mainContentColor) {
                        listPane(wide)
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
                        .consumeWindowInsets(insetPadding),
                ) {
                    CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
                        detailPane(wide)
                    }
                }
            }
        },
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .background(colors.containerColor),
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StyledSupportingPaneScaffold(
    mainPane: @Composable ColumnScope.(innerPadding: PaddingValues, wide: Boolean) -> Unit,
    supportingPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    colors: StyledPaneScaffoldColors = StyledPaneScaffoldDefaults.colors(),
    shouldAutoFocusCurrentDestination: Boolean = true,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalSpacing.current

    val defaultDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    val customDirective = remember(defaultDirective) {
        defaultDirective.copy(
            maxVerticalPartitions = 1,
            horizontalPartitionSpacerSize = spacing.windowPadding,
            shouldAutoFocusCurrentDestination = shouldAutoFocusCurrentDestination,
        )
    }
    val navigator = rememberSupportingPaneScaffoldNavigator(
        scaffoldDirective = customDirective,
    )
    val wide = remember(navigator.scaffoldState) {
        navigator.scaffoldState.targetState.secondary == PaneAdaptedValue.Expanded
    }
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues()

    SupportingPaneScaffold(
        directive = customDirective,
        scaffoldState = navigator.scaffoldState,
        mainPane = {
            AnimatedPane {
                val containerPadding = if (wide) {
                    PaddingValues(
                        start = insetPadding.calculateStartPadding(layoutDirection),
                        top = insetPadding.calculateTopPadding(),
                        bottom = insetPadding.calculateBottomPadding(),
                    )
                } else {
                    PaddingValues(
                        start = insetPadding.calculateStartPadding(layoutDirection),
                        top = insetPadding.calculateTopPadding(),
                        end = insetPadding.calculateEndPadding(layoutDirection),
                    )
                }
                // Don't apply bottom inset, but pass it down to mainPane, so that a composable within mainPane -- such
                // as MainCopySourceButton or ScaffoldAction -- can set a background under system navigation bar.
                val innerPadding = if (wide) {
                    PaddingValues.Zero
                } else {
                    PaddingValues(bottom = insetPadding.calculateBottomPadding())
                }
                Column(
                    Modifier
                        .background(if (wide) colors.wideMainContainerColor else colors.mainContainerColor)
                        .padding(containerPadding)
                        .consumeWindowInsets(insetPadding.minus(innerPadding))
                ) {
                    CompositionLocalProvider(LocalContentColor provides if (wide) colors.wideMainContentColor else colors.mainContentColor) {
                        mainPane(innerPadding, wide)
                    }
                }
            }
        },
        supportingPane = {
            AnimatedPane(Modifier.preferredWidth(450.dp)) {
                val containerPadding = if (wide) {
                    PaddingValues(
                        top = insetPadding.calculateTopPadding(),
                        end = insetPadding.calculateEndPadding(layoutDirection),
                        bottom = insetPadding.calculateBottomPadding(),
                    )
                } else {
                    PaddingValues(
                        start = insetPadding.calculateStartPadding(layoutDirection),
                        end = insetPadding.calculateEndPadding(layoutDirection),
                        bottom = insetPadding.calculateBottomPadding(),
                    )
                }
                Column(
                    Modifier
                        .padding(containerPadding)
                        .consumeWindowInsets(insetPadding)
                ) {
                    CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
                        supportingPane(wide)
                    }
                }
            }
        },
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .background(colors.containerColor),
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T> NavigableStyledListDetailPaneScaffold(
    navigator: ThreePaneScaffoldNavigator<T>,
    listPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    detailPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    colors: StyledPaneScaffoldColors = StyledPaneScaffoldDefaults.colors(),
    defaultBackBehavior: BackNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
) {
    ThreePaneScaffoldPredictiveBackHandler(
        navigator = navigator,
        backBehavior = defaultBackBehavior,
    )
    StyledListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        listPane = listPane,
        detailPane = detailPane,
        colors = colors,
    )
}
