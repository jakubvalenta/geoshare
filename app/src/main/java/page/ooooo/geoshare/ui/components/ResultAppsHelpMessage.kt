package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.lib.outputs.PointsOutput
import page.ooooo.geoshare.lib.outputs.SendPointOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.ui.FaqItemId
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

// TODO Extract
@Composable
fun ResultAppsHelpMessage(
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    sourceComesFromIntent: StateFlow<Boolean>,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onNavigateToFaqScreen: (itemId: FaqItemId?) -> Unit,
) {
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    if (sourceComesFromIntent) {
        val appName = stringResource(R.string.app_name)
        HelpMessageCard(
            helpMessage = HelpMessage.OPEN_BY_DEFAULT,
            dismissedHelpMessages = dismissedHelpMessages,
            title = { Text(stringResource(R.string.help_open_by_default_title, appName)) },
            actionText = {
                stringResource(R.string.help_open_by_default_action)
            },
            onAction = {
                onNavigateToFaqScreen(FaqItemId.OPEN_BY_DEFAULT)
            },
            onDismiss = onDismissHelpMessage,
        ) {
            ParagraphText(
                stringResource(R.string.help_open_by_default_text, appName)
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        ResultAppsHelpMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(true),
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkHelpPreview() {
    AppTheme {
        ResultAppsHelpMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(true),
            onDismissHelpMessage = {},
            onNavigateToFaqScreen = {},
        )
    }
}
