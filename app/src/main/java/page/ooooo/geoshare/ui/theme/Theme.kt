package page.ooooo.geoshare.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Immutable
data class ScreenshotColors(
    val textColor: Color,
    val mutedTextColor: Color,
)

val LocalScreenshotColors = staticCompositionLocalOf {
    ScreenshotColors(
        textColor = Color.Unspecified,
        mutedTextColor = Color.Unspecified,
    )
}

private val lightScreenshotColors = ScreenshotColors(
    textColor = screenshotTextColorLight,
    mutedTextColor = screenshotMutedTextColorLight,
)

private val darkScreenshotColors = ScreenshotColors(
    textColor = screenshotTextColorDark,
    mutedTextColor = screenshotMutedTextColorDark,
)

val LocalScreenshotTypography = staticCompositionLocalOf {
    ScreenshotTypography(
        textExtraExtraExtraLarge = TextStyle.Default,
        textExtraExtraLarge = TextStyle.Default,
        textExtraLarge = TextStyle.Default,
        textLarge = TextStyle.Default,
        textMedium = TextStyle.Default,
        textSmall = TextStyle.Default
    )
}

object ScreenshotTheme {
    val colors: ScreenshotColors
        @Composable
        get() = LocalScreenshotColors.current

    val typography: ScreenshotTypography
        @Composable
        get() = LocalScreenshotTypography.current
}

val LocalSpacing = staticCompositionLocalOf {
    Spacing()
}

@Composable
private fun isSmallWindow(): Boolean {
    val windowInfo = LocalWindowInfo.current
    val windowHeight = with(LocalDensity.current) { windowInfo.containerSize.height.toDp() }
    return windowHeight < 1200.dp
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    smallWindow: Boolean = isSmallWindow(),
    content: @Composable () -> Unit,
) {

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }
    val screenshotColors = if (darkTheme) darkScreenshotColors else lightScreenshotColors
    val typography = if (smallWindow) smallWindowTypography else defaultTypography
    val spacing = if (smallWindow) smallWindowSpacing else defaultSpacing

    CompositionLocalProvider(
        LocalScreenshotColors provides screenshotColors,
        LocalScreenshotTypography provides screenshotTypography,
        LocalSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
