package page.ooooo.geoshare.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.LocalSpacing

private fun createLinearFunc(x1: Float, y1: Float, x2: Float, y2: Float): (x: Int) -> Float {
    val a = (y2 - y1) / (x2 - x1)
    val b = y1 - a * x1
    return fun(x: Int) = a * x + b
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollablePane(
    @StringRes titleResId: Int,
    containerColor: Color = Color.Unspecified,
    onBack: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val spacing = LocalSpacing.current

    val scrollState = rememberScrollState()
    val headlineHeightPx = with(density) { MaterialTheme.typography.headlineMedium.fontSize.toPx() }
    val headlinePaddingTopPx = with(density) { spacing.large.toPx() }
    val headlinePaddingBottomPx = with(density) { spacing.medium.toPx() }
    val calcHeadlineAlpha = createLinearFunc(
        headlinePaddingTopPx, 1f,
        headlinePaddingTopPx + headlineHeightPx, 0f,
    )
    val calcTitleAlpha = createLinearFunc(
        headlinePaddingTopPx + headlineHeightPx, 0f,
        headlinePaddingTopPx + headlineHeightPx + headlinePaddingBottomPx, 1f,
    )
    val headlineAlpha by remember {
        derivedStateOf {
            calcHeadlineAlpha(scrollState.value).coerceIn(0f, 1f)
        }
    }
    val titleAlpha by remember {
        derivedStateOf {
            calcTitleAlpha(scrollState.value).coerceIn(0f, 1f)
        }
    }

    TopAppBar(
        title = {
            Text(
                stringResource(titleResId),
                Modifier.graphicsLayer { alpha = titleAlpha },
            )
        },
        navigationIcon = {
            if (onBack != null) {
                FilledIconButton(
                    onBack,
                    Modifier.testTag("geoShareBack"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back_content_description)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
    )
    Column(
        Modifier
            .background(containerColor)
            .fillMaxHeight()
            .verticalScroll(scrollState),
    ) {
        Headline(
            stringResource(titleResId),
            Modifier.graphicsLayer { alpha = headlineAlpha },
        )
        content()
    }
}
