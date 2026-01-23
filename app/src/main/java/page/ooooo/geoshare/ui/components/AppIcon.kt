package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import page.ooooo.geoshare.lib.android.AndroidTools

@Composable
fun AppIcon(packageName: String, size: Dp = 24.dp) {
    val context = LocalContext.current
    val appDetails = AndroidTools.queryAppDetails(context.packageManager, packageName)
    if (appDetails != null) {
        Image(
            rememberDrawablePainter(appDetails.icon),
            null,
            Modifier.widthIn(max = size),
        )
    }
}
