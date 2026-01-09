package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun AppHeadline(modifier: Modifier = Modifier, paid: Boolean = false) {
    val iconSize = with(LocalDensity.current) { MaterialTheme.typography.headlineLarge.fontSize.toDp() * 2f }
    val space = 4.dp

    Row(
        modifier.padding(end = iconSize - space),
        horizontalArrangement = Arrangement.spacedBy(space),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(R.string.about_app_icon_content_description),
            modifier = Modifier.requiredSize(iconSize),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(
            stringResource(if (paid) R.string.app_name_pro else R.string.app_name),
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
            AppHeadline()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            AppHeadline()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaidPreview() {
    AppTheme {
        Surface {
            AppHeadline(paid = true)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPaidPreview() {
    AppTheme {
        Surface {
            AppHeadline(paid = true)
        }
    }
}
