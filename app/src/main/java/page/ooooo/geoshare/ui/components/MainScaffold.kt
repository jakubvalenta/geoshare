package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScaffold(
    actions: @Composable () -> Unit,
    topContent: LazyListScope.() -> Unit,
    bottomContent: LazyListScope.() -> Unit,
    mainExpandable: Boolean,
    mainTitle: (@Composable (maxLines: Int) -> Unit)? = null,
    supportingTitle: (@Composable (maxLines: Int) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current

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
                expandable = mainExpandable,
                expandedHeight = if (wide) {
                    spacing.largeTopAppBarExpandedHeight
                } else {
                    spacing.largeTopAppBarExpandedHeight + spacing.medium
                },
            ) {
                topContent()
                if (!wide) {
                    item(key = "supporting_title", contentType = "column") {
                        val spacing = LocalSpacing.current
                        Column(
                            Modifier
                                .padding(horizontal = spacing.windowPadding)
                                .padding(top = spacing.small)
                        ) {
                            if (supportingTitle != null) {
                                supportingTitle(Int.MAX_VALUE)
                            }
                            // If there is no title, leave the empty column to create a space
                        }
                    }
                    bottomContent()
                }
            }
        },
        supportingPane = { wide ->
            if (wide) {
                TopAppBar(
                    title = {},
                    modifier = Modifier.testTag("geoShareMainSupportingPane"),
                    actions = {
                        if (wide) {
                            actions()
                        }
                    },
                )
                LazyColumn {
                    if (supportingTitle != null) {
                        item(key = "supporting_title", contentType = "column") {
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
