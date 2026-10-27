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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.di.fakeActivities
import page.ooooo.geoshare.data.di.getFakeAppDetails
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Geometries
import page.ooooo.geoshare.data.geo.WGS84Point
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.toAction
import page.ooooo.geoshare.data.preferences.HelpMessage
import page.ooooo.geoshare.data.preferences.isDismissed
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.ui.ResultAppsState
import page.ooooo.geoshare.ui.string.FormatArg
import page.ooooo.geoshare.ui.string.annotatedStringResource
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.defaultLinkStyles

@Composable
fun HelpShareSourceMessage(
    appDetails: StateFlow<AppDetails>,
    state: StateFlow<ResultAppsState>,
    dismissedHelpMessages: StateFlow<Set<HelpMessage>?>,
    sourceComesFromIntent: StateFlow<Boolean>,
    modifier: Modifier = Modifier,
    onDismissHelpMessage: (helpMessage: HelpMessage) -> Unit,
    onExecute: (action: Action<*>) -> Unit,
) {
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()

    if (!sourceComesFromIntent) {
        val helpMessage = HelpMessage.SHARE_SOURCE
        val appDetails by appDetails.collectAsStateWithLifecycle()
        val dismissedHelpMessages by dismissedHelpMessages.collectAsStateWithLifecycle()
        val state by state.collectAsStateWithLifecycle()
        val visible = !helpMessage.isDismissed(dismissedHelpMessages)

        HelpMessageCard(
            title = { Text(stringResource(R.string.help_share_source_title)) },
            visible = visible,
            modifier = modifier.testTag("geoShareHelpMessage_$helpMessage"),
            actionText = state.appsOutputs.exampleAppOutputs?.let { appOutputs ->
                {
                    stringResource(
                        R.string.help_share_source_action,
                        appDetails[appOutputs.packageName]?.label.orEmpty()
                    )
                }
            },
            onAction = {
                state.appsOutputs.exampleAppOutputs?.let { appOutputs ->
                    val examplePoint = WGS84Point.Kilimanjaro
                    onExecute(
                        appOutputs.defaultOutput.toAction(
                            point = examplePoint,
                            points = persistentListOf(examplePoint),
                            source = "",
                        )
                    )
                }
            },
            onDismissRequest = { onDismissHelpMessage(helpMessage) },
        ) {
            val shareIconId = "shareIcon"
            val shareIconSize = 14.sp
            ParagraphText(
                annotatedStringResource(
                    R.string.help_share_source_text,
                    defaultLinkStyles(),
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
        val uriQuote = DefaultUriQuote
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
            uriQuote = uriQuote,
        )
        HelpShareSourceMessage(
            appDetails = MutableStateFlow(getFakeAppDetails(context)),
            state = MutableStateFlow(
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(fakeActivities, emptySet())
                )
            ),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
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
        val uriQuote = DefaultUriQuote
        val outputRepository = OutputRepository(
            coordinateConverter = coordinateConverter,
            log = log,
            uriQuote = uriQuote,
        )
        HelpShareSourceMessage(
            appDetails = MutableStateFlow(getFakeAppDetails(context)),
            state = MutableStateFlow(
                ResultAppsState(
                    appsOutputs = outputRepository.getAppsOutputs(fakeActivities, emptySet())
                )
            ),
            dismissedHelpMessages = MutableStateFlow(emptySet()),
            sourceComesFromIntent = MutableStateFlow(false),
            onDismissHelpMessage = {},
            onExecute = {},
        )
    }
}
