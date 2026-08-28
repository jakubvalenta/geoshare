package page.ooooo.geoshare.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.OutputRepository
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

@Composable
fun BoxScope.WelcomeSheet(
    visible: StateFlow<Boolean>,
    appDetails: AppDetails,
    conversionSucceeded: Boolean,
    initialPage: Int = 0,
    sourceComesFromIntent: StateFlow<Boolean>,
    sourceMatchesInput: StateFlow<Boolean>,
    outputsForApps: Map<String, List<Output>>,
    onClose: () -> Unit,
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
            initialPage = initialPage,
            outputsForApps = outputsForApps,
            sourceComesFromIntent = sourceComesFromIntent,
            sourceMatchesInput = sourceMatchesInput,
            onClose = onClose,
        )
    }
}

@Composable
private fun WelcomeCard(
    appDetails: AppDetails,
    conversionSucceeded: Boolean,
    initialPage: Int = 0,
    outputsForApps: Map<String, List<Output>>,
    pageCount: Int = 4,
    sourceComesFromIntent: StateFlow<Boolean>,
    sourceMatchesInput: StateFlow<Boolean>,
    onClose: () -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    val examplePoint = WGS84Point.Kilimanjaro
    val insetPadding = WindowInsets.safeDrawing.asPaddingValues().run {
        PaddingValues(
            bottom = calculateBottomPadding(),
        )
    }
    val pagerState = rememberPagerState(initialPage) { pageCount }
    val animatedProgress by animateFloatAsState(
        targetValue = (pagerState.currentPage + 1f) / pageCount,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progressAnimation",
    )
    val sourceComesFromIntent by sourceComesFromIntent.collectAsStateWithLifecycle()
    val sourceMatchesInput by sourceMatchesInput.collectAsStateWithLifecycle()

    LaunchedEffect(conversionSucceeded, sourceComesFromIntent, sourceMatchesInput) {
        if (sourceComesFromIntent) {
            pagerState.animateScrollToPage(3)
        } else if (conversionSucceeded) {
            pagerState.animateScrollToPage(2)
        } else if (sourceMatchesInput) {
            pagerState.animateScrollToPage(1)
        }
    }

    ElevatedCard(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Box {
            Column(
                Modifier
                    .padding(insetPadding)
                    .consumeWindowInsets(insetPadding)
                    .padding(horizontal = spacing.windowPadding),
            ) {
                LinearProgressIndicator(
                    { animatedProgress },
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 20.dp, bottom = spacing.small),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
                    trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
                )
                HorizontalPager(pagerState, Modifier.padding(bottom = spacing.small)) { page ->
                    when (page) {
                        0 -> WelcomeStep(
                            page = 0,
                            pageCount = pageCount,
                            headline = { stringResource(R.string.welcome_paste_headline) },
                            text = { stringResource(R.string.welcome_paste_text, appName) },
                            action = {
                                ParagraphText(stringResource(R.string.welcome_paste_action))
                            },
                        ) {
                            ParagraphText(stringResource(R.string.welcome_paste_description))
                            SelectionContainer {
                                Text(
                                    UriFormatter.formatUriString(
                                        examplePoint, "https://maps.google.com/?q={lat}%2C{lon}"
                                    ).orEmpty(),
                                    Modifier
                                        .padding(horizontal = spacing.medium)
                                        .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)),
                                )
                            }
                        }

                        1 -> WelcomeStep(
                            page = 1,
                            pageCount = pageCount,
                            headline = { stringResource(R.string.welcome_submit_headline) },
                            text = { stringResource(R.string.welcome_submit_text) },
                            action = {
                                ParagraphText(
                                    annotatedStringResource(
                                        R.string.welcome_submit_action,
                                        FormatArg.Text(
                                            stringResource(R.string.main_create_geo_uri),
                                            SpanStyle(fontStyle = FontStyle.Italic),
                                        ),
                                    )
                                )
                            },
                        )

                        2 -> WelcomeStep(
                            page = 2,
                            pageCount = pageCount,
                            headline = { stringResource(R.string.welcome_share_headline) },
                            text = { stringResource(R.string.welcome_share_text, appName) },
                            action = {
                                ParagraphText(stringResource(R.string.welcome_share_action, appName))
                            },
                        ) {
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
                            if (exampleAppOutput != null) {
                                ParagraphText(
                                    buildAnnotatedString {
                                        append(stringResource(R.string.welcome_share_help))
                                        append(" ")
                                        val label =
                                            appDetails[exampleAppOutput.packageName]?.label.orEmpty()
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
                                                exampleAppOutput.toAction(examplePoint)
                                                    .execute(actionContext)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        3 -> WelcomeStep(
                            page = 3,
                            pageCount = pageCount,
                            headline = { stringResource(R.string.welcome_completed_headline) },
                            text = { stringResource(R.string.welcome_completed_text, appName) },
                            action = {
                                ParagraphText(stringResource(R.string.welcome_completed_action))
                            },
                        )
                    }
                }
            }
            FilledIconButton(
                onClose,
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 3.dp)
                    .alpha(0.5f),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiaryContainer,
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
    page: Int,
    pageCount: Int,
    headline: @Composable () -> String,
    text: (@Composable () -> String)? = null,
    action: @Composable () -> Unit,
    description: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current

    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Text(
            headline(),
            Modifier
                .padding(horizontal = 50.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall
        )
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            if (text != null) {
                ParagraphText(text())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                val image = when (page) {
                    pageCount - 1 -> painterResource(R.drawable.check_circle_24px)
                    0 -> painterResource(R.drawable.counter_1_24px)
                    1 -> painterResource(R.drawable.counter_2_24px)
                    2 -> painterResource(R.drawable.counter_3_24px)
                    3 -> painterResource(R.drawable.counter_4_24px)
                    else -> null
                }
                if (image != null) {
                    Icon(image, contentDescription = null)
                }
                Column(Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
                    CompositionLocalProvider(
                        LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                    ) {
                        action()
                    }
                    description?.invoke(this)
                }
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
                    initialPage = 0,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(false),
                    onClose = {},
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
                    initialPage = 0,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(false),
                    onClose = {},
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
                    initialPage = 0,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(false),
                    onClose = {},
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
                    initialPage = 2,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(true),
                    onClose = {},
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
                    initialPage = 2,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(true),
                    onClose = {},
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
                    conversionSucceeded = true,
                    initialPage = 2,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(true),
                    onClose = {},
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
                    conversionSucceeded = true,
                    initialPage = 2,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(false),
                    sourceMatchesInput = MutableStateFlow(true),
                    onClose = {},
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
                    conversionSucceeded = true,
                    initialPage = 3,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(true),
                    sourceMatchesInput = MutableStateFlow(true),
                    onClose = {},
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
                    conversionSucceeded = true,
                    initialPage = 3,
                    outputsForApps = outputRepository.getOutputsForApps(
                        mapOf(
                            PackageNames.OSMAND_PLUS to App(
                                packageName = PackageNames.OSMAND_PLUS,
                                dataTypes = setOf(DataType.GEO_URI)
                            ),
                        ),
                        hiddenApps = emptySet(),
                    ),
                    sourceComesFromIntent = MutableStateFlow(true),
                    sourceMatchesInput = MutableStateFlow(true),
                    onClose = {},
                )
            }
        }
    }
}
