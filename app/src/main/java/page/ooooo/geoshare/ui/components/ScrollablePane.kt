package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import page.ooooo.geoshare.R

// TODO Rename ScrollablePane to LargeTopBarPane
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollablePane(
    title: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.LargeAppBarExpandedHeight,
    navigationImageVector: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    content: @Composable ColumnScope.() -> Unit,
) {
    val appBarContainerColor = Color.Transparent
    val appBarContentColor = LocalContentColor.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Column(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        LargeTopAppBar(
            title = {
                title?.invoke()
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
            expandedHeight = expandedHeight,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = appBarContainerColor,
                scrolledContainerColor = appBarContainerColor,
                navigationIconContentColor = appBarContentColor,
                titleContentColor = appBarContentColor,
                actionIconContentColor = appBarContentColor,
                subtitleContentColor = appBarContentColor,
            ),
            // Don't set scrollBehavior when there is no title, otherwise the column is scrollable although there isn't
            // enough content.
            scrollBehavior = if (title != null) scrollBehavior else null,
        )
        content()
    }
}
