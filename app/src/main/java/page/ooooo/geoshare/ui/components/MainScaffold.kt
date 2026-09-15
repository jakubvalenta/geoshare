package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScaffold(
    actions: @Composable () -> Unit,
    topContent: LazyListScope.() -> Unit,
    bottomContent: LazyListScope.() -> Unit,
    mainExpandedHeight: Dp = LocalSpacing.current.largeTopAppBarExpandedHeight,
    mainTitle: (@Composable (maxLines: Int) -> Unit)? = null,
    supportingTitle: (@Composable (maxLines: Int) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    StyledSupportingPaneScaffold(
        mainPane = { innerPadding, wide ->
            LargeTopAppBarPane(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .testTag("geoShareMainPane"),
                title = mainTitle,
                onBack = onBack,
                actions = {
                    if (!wide) {
                        actions()
                    }
                },
                expandedHeight = mainExpandedHeight,
            ) {
                topContent()
                if (!wide) {
                    supportingTitle?.let { supportingTitle ->
                        item {
                            val spacing = LocalSpacing.current
                            Column(
                                Modifier
                                    .padding(horizontal = spacing.windowPadding)
                                    .padding(top = spacing.small)
                            ) {
                                supportingTitle(Int.MAX_VALUE)
                            }
                        }
                    }
                    bottomContent()
                }
            }
        },
        supportingPane = { wide ->
            LargeTopAppBarPane(
                modifier = Modifier.testTag("geoShareMainSupportingPane"),
                actions = {
                    if (wide) {
                        actions()
                    }
                },
            ) {
                if (wide) {
                    if (supportingTitle != null) {
                        item {
                            Column(Modifier.padding(horizontal = LocalSpacing.current.windowPadding)) {
                                supportingTitle(Int.MAX_VALUE)
                            }
                        }
                    }
                    bottomContent()
                }
            }
        },
        supportingPaneModifier = { Modifier.preferredWidth(0.5f) },
        shouldAutoFocusCurrentDestination = false,
    )
}
