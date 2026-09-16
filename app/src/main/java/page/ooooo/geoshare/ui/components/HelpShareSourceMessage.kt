package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.isMessagingApp
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.Action
import page.ooooo.geoshare.lib.outputs.PointOutput
import page.ooooo.geoshare.ui.OutputStatesForAppsByCategory
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.toOutputStatesForAppsByCategory

@Composable
fun HelpShareSourceMessage(
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    outputsForAppsByCategory: StateFlow<OutputStatesForAppsByCategory>,
    sourceComesFromIntent: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    val outputsForAppsByCategory by outputsForAppsByCategory.collectAsStateWithLifecycle()
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    if (!sourceComesFromIntent) {
        val examplePoint = WGS84Point.Kilimanjaro

        /**
         * An output that opens a point in a map app.
         *
         * The map app is the first installed app from a list of common map apps.
         */
        val (exampleAppLabel, exampleAppOutput) = setOf(
            PackageNames.GOOGLE_MAPS,
            PackageNames.OSMAND_PLUS,
            PackageNames.COMAPS_FDROID,
            PackageNames.ORGANIC_MAPS,
            PackageNames.MAPY_COM,
            PackageNames.HERE_WEGO,
            PackageNames.MAGIC_EARTH,
            PackageNames.MAPS_ME,
        ).firstNotNullOfOrNull { packageName ->
            outputsForAppsByCategory.mapApps.firstNotNullOfOrNull { outputState ->
                if (outputState.packageName == packageName) {
                    Pair(
                        outputState.label,
                        outputState.defaultOutputState.output as? PointOutput,
                    )
                } else {
                    null
                }
            }
        } ?: Pair(null, null)
        HelpMessageCard(
            helpMessage = HelpMessage.SHARE_SOURCE,
            dismissedHelpMessages = dismissedHelpMessages,
            title = { Text(stringResource(R.string.help_share_source_title)) },
            modifier = modifier,
            actionText = exampleAppLabel?.let { exampleAppLabel ->
                { stringResource(R.string.help_share_source_action, exampleAppLabel) }
            },
            onAction = {
                exampleAppOutput?.let { exampleAppOutput ->
                    onExecute(exampleAppOutput.toAction(examplePoint))
                }
            },
            onDismiss = onDismissHelpMessage,
        ) {
            val shareIconId = "shareIcon"
            val shareIconSize = 14.sp
            ParagraphText(
                annotatedStringResource(
                    R.string.help_share_source_text,
                    FormatArg.InlineContent(shareIconId),
                    FormatArg.Text(stringResource(R.string.app_name)),
                ),
                inlineContent = mapOf(
                    shareIconId to InlineTextContent(
                        Placeholder(
                            width = shareIconSize,
                            height = shareIconSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        )
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            Modifier.requiredSize(with(LocalDensity.current) { shareIconSize.toDp() }),
                        )
                    }
                )
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val log = DefaultLog
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
        )
        val appDetails = fakeAppDetails()
        HelpShareSourceMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            outputsForAppsByCategory = MutableStateFlow(
                fakeActivities
                    .partition { !it.isMessagingApp() }
                    .let { (mapAppActivities, messagingAppActivities) ->
                        Pair(
                            outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                            outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                        )
                    }
                    .toOutputStatesForAppsByCategory(appDetails)
            ),
            sourceComesFromIntent = MutableStateFlow(false),
            onDismissHelpMessage = {},
            onExecute = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        val context = LocalContext.current
        val geometries = Geometries(context)
        val coordinateConverter = CoordinateConverter(geometries)
        val log = DefaultLog
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
        )
        val appDetails = fakeAppDetails()
        HelpShareSourceMessage(
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            outputsForAppsByCategory = MutableStateFlow(
                fakeActivities
                    .partition { !it.isMessagingApp() }
                    .let { (mapAppActivities, messagingAppActivities) ->
                        Pair(
                            outputRepository.getOutputsForApps(mapAppActivities, hiddenApps = emptySet()),
                            outputRepository.getOutputsForApps(messagingAppActivities, hiddenApps = emptySet()),
                        )
                    }
                    .toOutputStatesForAppsByCategory(appDetails)
            ),
            sourceComesFromIntent = MutableStateFlow(false),
            onDismissHelpMessage = {},
            onExecute = {},
        )
    }
}
