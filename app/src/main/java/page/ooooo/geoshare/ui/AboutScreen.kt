package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.components.ScaffoldAction
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val billingStatus by viewModel.billingStatus.collectAsStateWithLifecycle()

    AboutScreen(
        billingStatus = billingStatus,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AboutScreen(
    billingStatus: BillingStatus,
    onBack: () -> Unit = {},
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val contentColor = MaterialTheme.colorScheme.onSurface
    val spacing = LocalSpacing.current
    val uriHandler = LocalUriHandler.current

    val defaultDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val customDirective = remember(defaultDirective) {
        defaultDirective.copy(
            maxVerticalPartitions = 2,
            verticalPartitionSpacerSize = 0.dp,
        )
    }
    val navigator = rememberSupportingPaneScaffoldNavigator(
        scaffoldDirective = customDirective,
    )
    val supportingReflowed = remember(navigator.scaffoldState) {
        navigator.scaffoldState.targetState.secondary is PaneAdaptedValue.Reflowed
    }

    SupportingPaneScaffold(
        directive = customDirective,
        scaffoldState = navigator.scaffoldState,
        mainPane = {
            AnimatedPane {
                Card(
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                ) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.about_title)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.nav_back_content_description)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    )
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .padding(horizontal = spacing.windowPadding)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = stringResource(R.string.about_app_icon_content_description),
                            modifier = Modifier
                                .size(144.dp)
                                .align(Alignment.CenterHorizontally),
                            colorFilter = ColorFilter.tint(LocalContentColor.current)
                        )
                        val appName = stringResource(R.string.app_name)
                        Text(
                            stringResource(
                                R.string.about_app_name_and_version,
                                appName,
                                BuildConfig.VERSION_NAME
                            ),
                            Modifier.padding(bottom = spacing.mediumAdaptive),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        ParagraphHtml(stringResource(R.string.about_text, appName))

                        if (billingStatus is BillingStatus.Purchased && billingStatus.product.type == BillingProduct.Type.DONATION) {
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                            ) {
                                Text(
                                    stringResource(R.string.about_text_google_play),
                                    Modifier.padding(spacing.small),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineBreak = LineBreak.Paragraph,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
        supportingPane = {
            if (billingStatus is BillingStatus.Purchased && billingStatus.product.type == BillingProduct.Type.DONATION) {
                val (contentHeight, setContentHeight) = remember { mutableStateOf<Dp?>(null) }
                val density = LocalDensity.current
                val spacing = LocalSpacing.current
                AnimatedPane(
                    Modifier
                        .preferredWidth(400.dp)
                        .run {
                            if (contentHeight != null) {
                                preferredHeight(contentHeight)
                            } else {
                                this
                            }
                        },
                ) {
                    Column(
                        modifier = Modifier
                            .run {
                                if (!supportingReflowed) {
                                    padding(
                                        start = spacing.windowPadding,
                                        top = spacing.builtInTopBarHeight,
                                        end = spacing.windowPadding
                                    )
                                } else {
                                    this
                                }
                            }
                            .onGloballyPositioned { coordinates ->
                                with(density) {
                                    setContentHeight(coordinates.size.height.toDp())
                                }
                            },
                    ) {
                        ScaffoldAction(
                            text = stringResource(R.string.donation_button),
                            onClick = {
                                uriHandler.openUri("https://ko-fi.com/jakubvalenta")
                            },
                            reflowed = supportingReflowed,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Text(
                                stringResource(R.string.donation_description),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .background(containerColor),
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        AboutScreen(
            billingStatus = BillingStatus.NotPurchased(),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        AboutScreen(
            billingStatus = BillingStatus.NotPurchased(),
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        AboutScreen(
            billingStatus = BillingStatus.NotPurchased(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DonationPreview() {
    AppTheme {
        AboutScreen(
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.DONATION),
                refundable = true,
            ),
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDonationPreview() {
    AppTheme {
        AboutScreen(
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.DONATION),
                refundable = true,
            ),
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletDonationPreview() {
    AppTheme {
        AboutScreen(
            billingStatus = BillingStatus.Purchased(
                product = BillingProduct("test", BillingProduct.Type.DONATION),
                refundable = true,
            ),
        )
    }
}
