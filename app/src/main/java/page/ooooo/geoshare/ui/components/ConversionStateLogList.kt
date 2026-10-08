package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeInputRepository
import page.ooooo.geoshare.data.local.preferences.Permission
import page.ooooo.geoshare.lib.Attempt
import page.ooooo.geoshare.lib.inputs.MatchedInput
import page.ooooo.geoshare.lib.network.ConnectTimeoutNetworkException
import page.ooooo.geoshare.lib.state.ConversionState
import page.ooooo.geoshare.lib.state.ExtendedStateLog
import page.ooooo.geoshare.lib.state.ExtendedStateLogItem
import page.ooooo.geoshare.lib.state.PermissionGrantedBasicInput
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

@Composable
fun ConversionStateLogList(
    expanded: Boolean,
    stateLog: StateFlow<ExtendedStateLog<ConversionState.HasDescription>>,
    animationsEnabled: Boolean = true,
    initialItemsExpanded: Boolean = false,
    onUriClick: (uriString: String) -> Unit,
) {
    val spacing = LocalSpacing.current

    val stateLog by stateLog.collectAsStateWithLifecycle()

    if (stateLog.isNotEmpty()) {
        AnimatedVisibility(
            expanded,
            // Fill max width to make the animation expand only vertically and not also horizontally
            modifier = Modifier.fillMaxWidth(),
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            SelectionContainer {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
                    Column(
                        Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.tiny),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        val enterAnimationFinished = transition.currentState == EnterExitState.Visible
                        stateLog.forEachIndexed { index, item ->
                            key(item.id) {
                                ConversionStateLogListItem(
                                    item = item,
                                    animationsEnabled = animationsEnabled && enterAnimationFinished,
                                    initialExpanded = initialItemsExpanded,
                                    onUriClick = onUriClick,
                                )
                            }
                            if (index < stateLog.size - 1) {
                                HorizontalDivider(Modifier.padding(vertical = spacing.tiny), thickness = Dp.Hairline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConversionStateLogListItem(
    item: ExtendedStateLogItem<ConversionState.HasDescription>,
    animationsEnabled: Boolean = true,
    iconSize: Dp = 16.dp,
    initialExpanded: Boolean = false,
    onUriClick: (uriString: String) -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    var visible by retain { mutableStateOf(!animationsEnabled) }

    // Trigger the enter animation on first composition
    LaunchedEffect(Unit) {
        if (animationsEnabled) {
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.extraTiny)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                when (item) {
                    is ExtendedStateLogItem.Finished ->
                        Icon(
                            if (item.succeeded) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize),
                        )

                    is ExtendedStateLogItem.Pending ->
                        LoadingIndicator(Modifier.size(iconSize), color = LocalContentColor.current)
                }
                Text(
                    item.state.getDescription(resources),
                    Modifier.weight(1f),
                )
                when (item) {
                    is ExtendedStateLogItem.Finished -> SecondsTimeText(item.end - item.start)
                    is ExtendedStateLogItem.Pending -> ElapsedTimeText(item.start)
                }
            }
            item.state.getDetails(resources)?.let { details ->
                ResultDetails(
                    details,
                    Modifier.padding(start = iconSize + spacing.tiny),
                    initialExpanded = initialExpanded
                )
            }
            item.state.uriString?.let { uriString ->
                Text(
                    uriString,
                    modifier = Modifier
                        .padding(start = iconSize + spacing.tiny)
                        .graphicsLayer { alpha = 0.9f }
                        .clickable { onUriClick(uriString) },
                    color = LocalContentColor.current.copy(alpha = 0.9f),
                    textDecoration = TextDecoration.Underline,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun fakeStateLog(source: String, timeSource: TestTimeSource): ExtendedStateLog<ConversionState.HasDescription> = listOf(
    ExtendedStateLogItem.Finished(
        id = 0,
        state = object : ConversionState, ConversionState.HasDescription {
            override fun getDescription(resources: Resources) =
                resources.getString(R.string.conversion_failed_reason_no_points)

            override fun getDetails(resources: Resources) =
                NotImplementedError().stackTraceToString()

            override val uriString = "41°24′12.2″N 2°10′26.5″E"
        },
        succeeded = false,
        start = timeSource.markNow(),
        end = timeSource.apply { plusAssign(5.milliseconds) }.markNow(),
    ),
    ExtendedStateLogItem.Finished(
        id = 1,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsShortLinkInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        ),
        succeeded = true,
        start = timeSource.markNow(),
        end = timeSource.apply { plusAssign(657.milliseconds) }.markNow(),
    ),
    ExtendedStateLogItem.Finished(
        id = 2,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsUriInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        ),
        succeeded = true,
        start = timeSource.markNow(),
        end = timeSource.apply { plusAssign(92.milliseconds) }.markNow(),
    ),
    ExtendedStateLogItem.Finished(
        id = 3,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
        ),
        succeeded = false,
        start = timeSource.markNow(),
        end = timeSource.apply { plusAssign(2011.milliseconds) }.markNow(),
    ),
    ExtendedStateLogItem.Pending(
        id = 4,
        state = PermissionGrantedBasicInput(
            source,
            matchedInput = MatchedInput(FakeInputRepository.googleMapsAddressApiInput, source),
            permission = Permission.ALWAYS,
            results = emptyMap(),
            lastAttempt = Attempt(1, ConnectTimeoutNetworkException(Exception())),
        ),
        start = timeSource.markNow(),
    ),
)

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val timeSource = TestTimeSource()
            ConversionStateLogList(
                expanded = true,
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
                animationsEnabled = false,
                initialItemsExpanded = true,
                onUriClick = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val source = "https://maps.app.goo.gl/TmbeHMiLEfTBws9EA"
            val timeSource = TestTimeSource()
            ConversionStateLogList(
                expanded = true,
                stateLog = MutableStateFlow(fakeStateLog(source, timeSource)),
                animationsEnabled = false,
                initialItemsExpanded = true,
                onUriClick = {},
            )
        }
    }
}
