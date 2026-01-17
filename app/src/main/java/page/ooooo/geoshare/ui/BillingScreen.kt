package page.ooooo.geoshare.ui

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.FakeOneTimeOffer
import page.ooooo.geoshare.lib.billing.FakeSubscriptionOffer
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.ui.components.AppHeadline
import page.ooooo.geoshare.ui.components.LargeButton
import page.ooooo.geoshare.ui.components.TextList
import page.ooooo.geoshare.ui.components.TextListBullet
import page.ooooo.geoshare.ui.components.TextListItem
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

@Composable
fun BillingScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val billingAppNameResId = viewModel.billingAppNameResId
    val billingErrorMessageResId by viewModel.billingErrorMessageResId.collectAsStateWithLifecycle()
    val billingFeatures = viewModel.billingFeatures
    val billingOffers by viewModel.billingOffers.collectAsStateWithLifecycle()
    val billingRefundableDuration = viewModel.billingRefundableDuration
    val billingStatus by viewModel.billingStatus.collectAsStateWithLifecycle()

    BillingScreen(
        billingAppNameResId = billingAppNameResId,
        billingErrorMessageResId = billingErrorMessageResId,
        billingFeatures = billingFeatures,
        billingOffers = billingOffers,
        billingRefundableDuration = billingRefundableDuration,
        billingStatus = billingStatus,
        onBack = onBack,
        onLaunchBillingFlow = { offerToken ->
            viewModel.launchBillingFlow(context as Activity, offerToken)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillingScreen(
    billingAppNameResId: Int,
    @StringRes billingErrorMessageResId: Int?,
    billingFeatures: List<Feature>,
    billingOffers: List<Offer>,
    billingRefundableDuration: Duration,
    billingStatus: BillingStatus,
    onBack: () -> Unit,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalSpacing.current
    val expanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        if (expanded) {
            Row(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                Column(Modifier.weight(0.6f)) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        BillingFirstPane(billingAppNameResId, billingFeatures, billingStatus)
                    }
                }
                Column(Modifier.weight(0.4f)) {
                    ElevatedCard(
                        Modifier.padding(top = spacing.smallAdaptive, end = spacing.windowPadding),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        BillingSecondPane(
                            billingErrorMessageResId,
                            billingOffers,
                            billingRefundableDuration,
                            billingStatus,
                            onLaunchBillingFlow
                        )
                    }
                }
            }
        } else {
            val innerPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection),
            )
            Column(
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    BillingFirstPane(billingAppNameResId, billingFeatures, billingStatus)
                }
                ElevatedCard(
                    shape = MaterialTheme.shapes.large.copy(
                        bottomStart = ZeroCornerSize,
                        bottomEnd = ZeroCornerSize,
                    ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 20.dp)
                ) {
                    BillingSecondPane(
                        billingErrorMessageResId,
                        billingOffers,
                        billingRefundableDuration,
                        billingStatus,
                        onLaunchBillingFlow
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BillingFirstPane(
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingStatus: BillingStatus,
) {
    val spacing = LocalSpacing.current

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (billingStatus is BillingStatus.Loading) {
            LoadingIndicator(
                Modifier.size(96.dp),
                color = MaterialTheme.colorScheme.tertiary,
            )
        } else {
            Card(
                Modifier.padding(top = spacing.largeAdaptive),
                shape = MaterialShapes.Sunny.toShape(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Icon(
                    painterResource(R.drawable.crown_24px),
                    null,
                    Modifier
                        .padding(15.dp)
                        .requiredSize(48.dp),
                )
            }
            Text(
                stringResource(
                    if (billingStatus is BillingStatus.Purchased) {
                        R.string.billing_intro_purchased
                    } else {
                        R.string.billing_intro_not_purchased
                    },
                    R.string.billing_intro_not_purchased
                ),
                Modifier.padding(top = spacing.mediumAdaptive),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            AppHeadline(billingAppNameResId, Modifier.padding(top = spacing.smallAdaptive), iconEnabled = false)
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                    lineBreak = LineBreak.Paragraph,
                    hyphens = Hyphens.Auto,
                )
            ) {
                TextList(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = spacing.small,
                            top = spacing.largeAdaptive,
                            end = spacing.medium,
                        ),
                    bulletSpace = spacing.tiny,
                    bulletWidth = 44.dp,
                ) {
                    TextListItem(
                        Modifier.padding(bottom = spacing.smallAdaptive),
                        bullet = {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    ) {
                        Text(
                            stringResource(R.string.billing_feature_all),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    billingFeatures.forEach { feature ->
                        TextListItem(
                            Modifier.padding(bottom = spacing.smallAdaptive),
                            bullet = {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        ) {
                            Text(
                                stringResource(feature.titleResId),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        TextListItem(Modifier.padding(bottom = spacing.smallAdaptive)) {
                            Text(stringResource(feature.descriptionResId))
                        }
                        feature.itemsResIds.forEach { itemResId ->
                            TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                                Text(stringResource(itemResId))
                            }
                        }
                    }
                }
            }
            if (billingStatus is BillingStatus.Purchased && billingStatus.product.type != BillingProduct.Type.DONATION) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    LocalTextStyle provides MaterialTheme.typography.bodySmall,
                ) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = spacing.windowPadding, vertical = spacing.largeAdaptive),
                        thickness = Dp.Hairline,
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.windowPadding + 18.dp),
                        verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)
                    ) {
                        Text(buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "https://geoshare.ooooo.page/terms/",
                                    TextLinkStyles(
                                        SpanStyle(textDecoration = TextDecoration.Underline)
                                    )
                                )
                            ) {
                                append(stringResource(R.string.billing_terms_of_service))
                            }
                        })
                        Text(buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "mailto:geoshare-support@jakubvalenta.cz",
                                    TextLinkStyles(
                                        SpanStyle(textDecoration = TextDecoration.Underline)
                                    )
                                )
                            ) {
                                append(stringResource(R.string.billing_support_email))
                            }
                        })
                    }
                }
            }
            // TODO Show success message
            // TODO Show subscription error: https://developer.android.com/google/play/billing/subscriptions#in-app-messaging
        }
    }
}

@Composable
private fun BillingSecondPane(
    @StringRes billingErrorMessageResId: Int?,
    billingOffers: List<Offer>,
    billingRefundableDuration: Duration,
    billingStatus: BillingStatus,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val uriHandler = LocalUriHandler.current
    var selectedOffer by remember { mutableStateOf(billingOffers.firstOrNull()) }

    if (billingErrorMessageResId != null) {
        Column(
            Modifier.safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(spacing.small),
                shape = MaterialTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text(
                    stringResource(billingErrorMessageResId),
                    Modifier.padding(spacing.small),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
    when (billingStatus) {
        is BillingStatus.NotPurchased -> {
            Column(
                Modifier
                    .safeDrawingPadding()
                    .padding(vertical = spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                ElevatedCard(
                    Modifier
                        .selectableGroup()
                        .padding(horizontal = spacing.small),
                ) {
                    billingOffers.forEachIndexed { i, offer ->
                        ListItem(
                            headlineContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = offer == selectedOffer,
                                        // Null recommended for accessibility with screen readers
                                        onClick = null,
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            when (offer.period) {
                                                Offer.Period.ONE_TIME -> stringResource(R.string.billing_one_time)
                                                Offer.Period.MONTHLY -> stringResource(R.string.billing_subscription_monthly)
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                        Text(
                                            when (offer.period) {
                                                Offer.Period.ONE_TIME -> stringResource(
                                                    R.string.billing_refund_description,
                                                    billingRefundableDuration.toInt(DurationUnit.HOURS),
                                                )

                                                Offer.Period.MONTHLY -> stringResource(R.string.billing_subscription_cancel_description)
                                            },
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Text(
                                        offer.formattedPrice,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            },
                            modifier = Modifier
                                .selectable(
                                    selected = offer == selectedOffer,
                                    role = Role.RadioButton,
                                    onClick = { selectedOffer = offer },
                                ),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        )
                        if (i < billingOffers.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                        }
                    }
                }
                LargeButton(
                    stringResource(R.string.billing_purchase_button),
                    containerColor = if (selectedOffer != null) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = if (selectedOffer != null) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ) {
                    selectedOffer?.let { selectedOffer ->
                        onLaunchBillingFlow(selectedOffer.token)
                    }
                }
            }
        }

        is BillingStatus.Purchased -> billingStatus.product.let { product ->
            Column(
                Modifier
                    .safeDrawingPadding()
                    .padding(vertical = spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                when (product.type) {
                    BillingProduct.Type.DONATION -> stringResource(
                        R.string.donation_description,
                        stringResource(R.string.app_name)
                    )

                    BillingProduct.Type.ONE_TIME if billingStatus.refundable -> stringResource(
                        R.string.billing_refund_description,
                        billingRefundableDuration.toInt(DurationUnit.HOURS)
                    )

                    BillingProduct.Type.ONE_TIME -> null
                    BillingProduct.Type.SUBSCRIPTION -> stringResource(R.string.billing_manage_subscription_description)
                }?.let { description ->
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                LargeButton(
                    stringResource(
                        when (product.type) {
                            BillingProduct.Type.DONATION -> R.string.donation_button
                            BillingProduct.Type.ONE_TIME -> R.string.billing_order_history
                            BillingProduct.Type.SUBSCRIPTION -> R.string.billing_manage_subscription
                        }
                    ),
                ) {
                    @Suppress("SpellCheckingInspection")
                    val uri = when (product.type) {
                        BillingProduct.Type.DONATION -> "https://ko-fi.com/jakubvalenta"
                        BillingProduct.Type.ONE_TIME -> "https://play.google.com/store/account/orderhistory"
                        BillingProduct.Type.SUBSCRIPTION -> ("https://play.google.com/store/account/subscriptions"
                            + "?sku=${Uri.encode(product.id)}&package=page.ooooo.geoshare")
                    }
                    uriHandler.openUri(uri)
                }
            }
        }

        is BillingStatus.Loading -> {}
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedDonationPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedDonationPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedDonationPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedOneTimePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedOneTimePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedOneTimePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedOneTimeNotRefundablePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                false,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedOneTimeNotRefundablePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                false,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedOneTimeNotRefundablePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                false,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedSubscriptionPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedSubscriptionPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedSubscriptionPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = R.string.billing_purchase_error_cancelled,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = R.string.billing_purchase_error_cancelled,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletErrorPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = R.string.billing_purchase_error_cancelled,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingErrorMessageResId = null,
            billingFeatures = listOf(AutomationFeature),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}
