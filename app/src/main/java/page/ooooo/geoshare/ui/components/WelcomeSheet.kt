package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.lib.outputs.ActionContext
import page.ooooo.geoshare.lib.outputs.OpenPointOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration.Companion.seconds

@Composable
fun BoxScope.WelcomeSheet(
    visible: StateFlow<Boolean>,
    appDetails: AppDetails,
    conversionSucceeded: Boolean,
    initialLinkCopied: Boolean = false,
    source: StateFlow<String>,
    sourceComesFromIntent: StateFlow<Boolean>,
    outputsForApps: Map<String, List<Output>>,
    onClose: () -> Unit,
    onTextMatchesInput: (text: String) -> Boolean,
) {
    val visible by visible.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible,
        Modifier
            .align(Alignment.BottomCenter)
            .widthIn(max = 600.dp),
        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
        exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
    ) {
        WelcomeCard(
            conversionSucceeded = conversionSucceeded,
            appDetails = appDetails,
            initialLinkCopied = initialLinkCopied,
            outputsForApps = outputsForApps,
            source = source,
            sourceComesFromIntent = sourceComesFromIntent,
            onClose = onClose,
            onTextMatchesInput = onTextMatchesInput,
        )
    }
}

@Composable
private fun WelcomeCard(
    appDetails: AppDetails,
    conversionSucceeded: Boolean,
    initialLinkCopied: Boolean = false,
    outputsForApps: Map<String, List<Output>>,
    stepCount: Int = 3,
    source: StateFlow<String>,
    sourceComesFromIntent: StateFlow<Boolean>,
    onClose: () -> Unit,
    onTextMatchesInput: (text: String) -> Boolean,
) {
    val appName = stringResource(R.string.app_name)
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

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

    val source by source.collectAsStateWithLifecycle()
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()
    val sourceIsNotEmpty = remember(source) { source.isNotEmpty() }
    var linkCopied by remember { mutableStateOf(initialLinkCopied) }
    val completedIndex = remember(conversionSucceeded, sourceIsNotEmpty, linkCopied) {
        if (sourceComesFromIntent) {
            32
        } else if (conversionSucceeded) {
            2
        } else if (sourceIsNotEmpty) {
            1
        } else if (linkCopied) {
            0
        } else {
            -1
        }
    }
    val allStepsCompleted = completedIndex >= stepCount - 1

    LaunchedEffect(conversionSucceeded, sourceIsNotEmpty) {
        if (!conversionSucceeded && !sourceIsNotEmpty) {
            while (!linkCopied) {
                if (
                    AndroidTools
                        .silentPasteFromClipboard(clipboard)
                        .let { it.isNotEmpty() && onTextMatchesInput(it) }
                ) {
                    linkCopied = true
                } else {
                    delay(1.seconds)
                }
            }
        }
    }

    ElevatedCard(
        Modifier.fillMaxWidth(), // TODO Apply window padding
        shape = MaterialTheme.shapes.large.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Box {
            Column(Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.small + spacing.tiny)) {
                Text(
                    if (allStepsCompleted) {
                        stringResource(R.string.welcome_completed_headline, appName)
                    } else {
                        stringResource(R.string.welcome_headline, appName)
                    },
                    Modifier
                        .padding(horizontal = 50.dp)
                        .padding(bottom = spacing.small)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                    if (allStepsCompleted) {
                        ParagraphText(
                            stringResource(R.string.welcome_completed_text),
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        ParagraphText(
                            stringResource(R.string.welcome_text, appName),
                        )
                    }
                }
                Column(
                    Modifier.padding(top = spacing.small),
                    verticalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    WelcomeStep(
                        index = 0,
                        completedIndex = completedIndex,
                        title = { ParagraphText(stringResource(R.string.welcome_copy)) },
                    ) {
                        ParagraphText(stringResource(R.string.welcome_copy_help))
                        SelectionContainer {
                            Text(
                                UriFormatter.formatUriString(
                                    examplePoint, "https://maps.google.com/?q={lat}%2C{lon}"
                                ).orEmpty(),
                                Modifier.background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                            )
                        }
                    }
                    WelcomeStep(
                        index = 1,
                        completedIndex = completedIndex,
                        title = { ParagraphText(stringResource(R.string.welcome_paste)) },
                    )
                    WelcomeStep(
                        index = 2, completedIndex = completedIndex,
                        title = {
                            ParagraphText(
                                annotatedStringResource(
                                    R.string.welcome_submit,
                                    FormatArg.Text(
                                        stringResource(R.string.main_create_geo_uri),
                                        SpanStyle(fontStyle = FontStyle.Italic),
                                    )
                                )
                            )
                        },
                    )
                    WelcomeStep(
                        index = 3,
                        completedIndex = completedIndex,
                        title = {
                            ParagraphText(stringResource(R.string.welcome_share, appName))
                        },
                    ) {
                        if (exampleAppOutput != null) {
                            ParagraphText(
                                buildAnnotatedString {
                                    append(stringResource(R.string.welcome_share_help))
                                    append(" ")
                                    val label = appDetails[exampleAppOutput.packageName]?.label.orEmpty()
                                    ClickableLink(
                                        stringResource(R.string.welcome_share_open_app, label),
                                        styles = AnnotatedString.UnderlinedLinkStyles,
                                    ) {
                                        val actionContext = ActionContext(
                                            context = context,
                                            clipboard = clipboard,
                                            resources = resources,
                                        )
                                        coroutineScope.launch {
                                            exampleAppOutput.toAction(examplePoint).execute(actionContext)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            FilledIconButton(
                onClose,
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 5.dp)
                    .alpha(0.8f),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.intro_nav_close),
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(
    index: Int,
    completedIndex: Int,
    title: @Composable () -> Unit,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    val stepCompleted = completedIndex >= index

    Row(
        Modifier.graphicsLayer {
            alpha = if (stepCompleted) 0.7f else 1f
        },
        horizontalArrangement = Arrangement.spacedBy(spacing.tiny),
    ) {
        Crossfade(stepCompleted) { targetState ->
            if (targetState) {
                Icon(painterResource(R.drawable.check_circle_24px), contentDescription = null)
            } else {
                when (index) {
                    0 -> painterResource(R.drawable.counter_1_24px)
                    1 -> painterResource(R.drawable.counter_2_24px)
                    2 -> painterResource(R.drawable.counter_3_24px)
                    3 -> painterResource(R.drawable.counter_4_24px)
                    else -> null
                }?.let { painter -> Icon(painter, contentDescription = null) }
            }
        }
        Column(Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.run {
                    if (!stepCompleted) {
                        copy(fontWeight = FontWeight.Bold)
                    } else {
                        this
                    }
                }) {
                    title()
                }
                content?.invoke(this)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FirstStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFirstStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletFirstStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    initialLinkCopied = true,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSecondStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    initialLinkCopied = true,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThirdStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow("foo"),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkThirdStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow("foo"),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FourthStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow("foo"),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkFourthStepPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = false,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow("foo"),
                    sourceComesFromIntent = MutableStateFlow(false),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletedPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = true,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(true),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkCompletedPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
            ) {
                val context = LocalContext.current
                val geometries = Geometries(context)
                val coordinateConverter = CoordinateConverter(geometries)
                val outputRepository = OutputRepository(
                    coordinateConverter = coordinateConverter,
                )
                @SuppressLint("LocalContextGetResourceValueCall")
                WelcomeSheet(
                    visible = MutableStateFlow(true),
                    appDetails = mapOf(
                        PackageNames.OSMAND_PLUS to AppDetail(
                            packageName = PackageNames.OSMAND_PLUS,
                            label = "OsmAnd",
                            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!
                        ),
                    ),
                    conversionSucceeded = true,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    source = MutableStateFlow(""),
                    sourceComesFromIntent = MutableStateFlow(true),
                    onClose = {},
                    onTextMatchesInput = { false },
                )
            }
        }
    }
}
