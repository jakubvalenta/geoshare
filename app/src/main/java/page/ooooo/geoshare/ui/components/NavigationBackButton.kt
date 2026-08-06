package page.ooooo.geoshare.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R

@Composable
fun NavigationBackButton(onClick: () -> Unit, imageVector: ImageVector = Icons.AutoMirrored.Default.ArrowBack) {
    IconButton(onClick, Modifier.testTag("geoShareBack")) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(R.string.nav_back_content_description),
        )
    }
}
