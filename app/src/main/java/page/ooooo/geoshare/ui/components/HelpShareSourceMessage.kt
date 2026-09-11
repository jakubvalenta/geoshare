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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.di.fakeApps
import page.ooooo.geoshare.data.local.preferences.HelpMessage
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionContext
import page.ooooo.geoshare.lib.outputs.OpenPointOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.ui.theme.AppTheme

@Composable
fun HelpShareSourceMessage(
    appDetails: StateFlow<AppDetails>,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    outputsForApps: StateFlow<Map<String, List<Output>>>,
    sourceComesFromIntent: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current

    val appDetails by appDetails.collectAsStateWithLifecycle()
    val outputsForApps by outputsForApps.collectAsStateWithLifecycle()
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    if (!sourceComesFromIntent) {
        val examplePoint = WGS84Point.Kilimanjaro

        /**
         * An output that opens a point in a map app.
         *
         * The map app is the first installed app from a list of common map apps.
         */
        val exampleAppOutput = setOf(
            PackageNames.GOOGLE_MAPS,
            PackageNames.OSMAND_PLUS,
            PackageNames.COMAPS_FDROID,
            PackageNames.ORGANIC_MAPS,
            PackageNames.MAPY_COM,
            PackageNames.HERE_WEGO,
            PackageNames.MAGIC_EARTH,
            PackageNames.MAPS_ME,
        ).firstNotNullOfOrNull { packageName ->
            outputsForApps[packageName]?.firstNotNullOfOrNull { it as? OpenPointOutput }
        }
        HelpMessageCard(
            helpMessage = HelpMessage.SHARE_SOURCE,
            dismissedHelpMessages = dismissedHelpMessages,
            title = { Text(stringResource(R.string.help_share_source_title)) },
            modifier = modifier,
            actionText = exampleAppOutput?.let { exampleAppOutput ->
                appDetails[exampleAppOutput.packageName]?.label?.let { exampleAppLabel ->
                    {
                        stringResource(R.string.help_share_source_action, exampleAppLabel)
                    }
                }
            },
            onAction = {
                exampleAppOutput?.let { exampleAppOutput ->
                    val actionContext = ActionContext(
                        context = context, clipboard = clipboard, resources = resources
                    )
                    coroutineScope.launch {
                        exampleAppOutput.toAction(examplePoint).execute(actionContext)
                    }
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
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        HelpShareSourceMessage(
            appDetails = MutableStateFlow(fakeAppDetails()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            outputsForApps = MutableStateFlow(
                outputRepository.getOutputsForApps(
                    apps = fakeApps,
                    hiddenApps = emptySet(),
                )
            ),
            sourceComesFromIntent = MutableStateFlow(false),
            onDismissHelpMessage = {},
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
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
        )
        HelpShareSourceMessage(
            appDetails = MutableStateFlow(fakeAppDetails()),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            outputsForApps = MutableStateFlow(
                outputRepository.getOutputsForApps(
                    apps = fakeApps,
                    hiddenApps = emptySet(),
                )
            ),
            sourceComesFromIntent = MutableStateFlow(false),
            onDismissHelpMessage = {},
        )
    }
}
