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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

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

// TODO Rename BasicListDetailScaffold to TwoPaneSupportingPaneScaffold
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun BasicSupportingPaneScaffold(
    mainPane: @Composable ColumnScope.(innerPadding: PaddingValues, wide: Boolean) -> Unit,
    supportingPane: @Composable ColumnScope.(wide: Boolean) -> Unit,
    colors: TwoPaneScaffoldColors = TwoPaneScaffoldDefaults.colors(),
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
