package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R

@Composable
fun ResultPointCopyButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick,
        modifier
            .width(36.dp)
            .height(28.dp) // Set height to adjust default vertical padding
    ) {
        Icon(
            painterResource(R.drawable.content_copy_24px),
            contentDescription = stringResource(R.string.copy),
        )
    }
}

@Composable
fun ResultPointSaveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick,
        modifier
            .width(36.dp)
            .height(28.dp) // Set height to adjust default vertical padding
    ) {
        Icon(
            painterResource(R.drawable.download_24px),
            contentDescription = stringResource(R.string.save),
        )
    }
}

@Composable
fun ResultPointShareButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick,
        modifier
            .width(30.dp)
            .height(28.dp) // Set height to adjust default vertical padding
    ) {
        Icon(
            Icons.Default.Share,
            contentDescription = stringResource(R.string.share),
            modifier = Modifier.size(20.dp),
        )
    }
}
