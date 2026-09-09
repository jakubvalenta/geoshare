package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainSupportingPaneScaffold(
    actions: @Composable () -> Unit,
    topContent: LazyListScope.(innerPadding: PaddingValues) -> Unit,
    bottomContent: LazyListScope.(innerPadding: PaddingValues) -> Unit,
    mainExpandedHeight: Dp = LocalSpacing.current.largeTopAppBarExpandedHeight,
    mainTitle: (@Composable (maxLines: Int) -> Unit)? = null,
    supportingTitle: (@Composable (maxLines: Int) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    StyledSupportingPaneScaffold(
        mainPane = { innerPadding, wide ->
            // TODO Column(Modifier.weight(1f)) {
            LargeTopAppBarPane(
                modifier = Modifier.testTag("geoShareMainPane"),
                title = mainTitle,
                onBack = onBack,
                actions = {
                    if (!wide) {
                        actions()
                    }
                },
                expandedHeight = mainExpandedHeight,
            ) {
                topContent(innerPadding)
                if (!wide) {
                    supportingTitle?.let { supportingTitle ->
                        item {
                            val spacing = LocalSpacing.current
                            Column(
                                Modifier
                                    .padding(horizontal = spacing.windowPadding)
                                    .padding(top = spacing.medium)
                            ) {
                                supportingTitle(Int.MAX_VALUE)
                            }
                        }
                    }
                    bottomContent(innerPadding)
                }
            }
            // }
        },
        supportingPane = { wide ->
            LargeTopAppBarPane(
                modifier = Modifier.testTag("geoShareMainSupportingPane"),
                title = supportingTitle,
                actions = {
                    if (wide) {
                        actions()
                    }
                },
            ) {
                if (wide) {
                    bottomContent(PaddingValues.Zero)
                }
            }
        },
        shouldAutoFocusCurrentDestination = false,
    )
}
