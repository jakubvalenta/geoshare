package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun AppHeadline(@StringRes appNameResId: Int, modifier: Modifier = Modifier, iconEnabled: Boolean = true) {
    val iconSize = with(LocalDensity.current) { MaterialTheme.typography.headlineLarge.fontSize.toDp() * 2f }
    val space = 4.dp

    Row(
        modifier.run {
            if (iconEnabled) {
                this.padding(end = iconSize - space)
            } else {
                this
            }
        },
        horizontalArrangement = Arrangement.spacedBy(space),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconEnabled) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.about_app_icon_content_description),
                modifier = Modifier.requiredSize(iconSize),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )
        }
        Text(
            stringResource(appNameResId),
            Modifier.testTag("geoShareAppHeadlineText"),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            AppHeadline(appNameResId = R.string.app_name)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            AppHeadline(appNameResId = R.string.app_name)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedPreview() {
    AppTheme {
        Surface {
            AppHeadline(appNameResId = R.string.app_name_pro)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedPreview() {
    AppTheme {
        Surface {
            AppHeadline(appNameResId = R.string.app_name_pro)
        }
    }
}
