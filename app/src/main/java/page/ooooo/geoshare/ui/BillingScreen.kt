package page.ooooo.geoshare.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.FakeOneTimeOffer
import page.ooooo.geoshare.lib.billing.FakeSubscriptionOffer
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.ui.components.AppHeadline
import page.ooooo.geoshare.ui.components.BasicSupportingPaneScaffold
import page.ooooo.geoshare.ui.components.ScaffoldAction
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
    val billingFeatures = viewModel.billingFeatures
    val billingMessage by viewModel.billingMessage.collectAsStateWithLifecycle()
    val billingOffers by viewModel.billingOffers.collectAsStateWithLifecycle()
    val billingRefundableDuration = viewModel.billingRefundableDuration
    val billingStatus by viewModel.billingStatus.collectAsStateWithLifecycle()

    BillingScreen(
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingMessage = billingMessage,
        billingOffers = billingOffers,
        billingRefundableDuration = billingRefundableDuration,
        billingStatus = billingStatus,
        onBack = onBack,
        onLaunchBillingFlow = { offerToken ->
            viewModel.launchBillingFlow(context as Activity, offerToken)
        },
        onManageBillingProduct = { product ->
            viewModel.manageBillingProduct(product)
        },
    )
}

@Composable
private fun BillingScreen(
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingMessage: Message?,
    billingOffers: List<Offer>,
    billingRefundableDuration: Duration,
    billingStatus: BillingStatus,
    onBack: () -> Unit,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
    onManageBillingProduct: (product: BillingProduct) -> Unit,
) {
    val spacing = LocalSpacing.current

    BasicSupportingPaneScaffold(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back_content_description)
                )
            }
        },
        mainPane = { innerPadding, wide ->
            Column(
                Modifier
                    .weight(1f)
                    .run {
                        if (wide) {
                            padding(innerPadding)
                            consumeWindowInsets(innerPadding)
                        } else {
                            this
                        }
                    }
                    .verticalScroll(rememberScrollState()),
            ) {
                BillingMainPane(
                    billingAppNameResId,
                    billingFeatures,
                    billingMessage,
                    billingStatus,
                )
            }
            if (!wide) {
                BillingSupportingPane(
                    billingOffers = billingOffers,
                    billingRefundableDuration = billingRefundableDuration,
                    billingStatus = billingStatus,
                    innerPadding = innerPadding,
                    bottomCorners = false,
                    onLaunchBillingFlow = onLaunchBillingFlow,
                    onManageBillingProduct = onManageBillingProduct,
                )
            }
        },
        supportingPane = {
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                BillingSupportingPane(
                    billingOffers = billingOffers,
                    billingRefundableDuration = billingRefundableDuration,
                    billingStatus = billingStatus,
                    innerPadding = PaddingValues.Zero,
                    bottomCorners = true,
                    onLaunchBillingFlow = onLaunchBillingFlow,
                    onManageBillingProduct = onManageBillingProduct,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BillingMainPane(
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingMessage: Message?,
    billingStatus: BillingStatus,
) {
    val spacing = LocalSpacing.current

    if (billingMessage != null) {
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (billingMessage.isError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (billingMessage.isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
            ),
        ) {
            Text(
                billingMessage.text,
                Modifier
                    .testTag(
                        if (!billingMessage.isError) {
                            "geoShareBillingMessageSuccess"
                        } else {
                            "geoShareBillingMessageError"
                        }
                    )
                    .padding(spacing.small),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
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
                    R.string.billing_intro_not_purchased,
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
            if (billingStatus is BillingStatus.Purchased) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    LocalTextStyle provides MaterialTheme.typography.bodySmall,
                ) {
                    HorizontalDivider(
                        Modifier
                            .padding(horizontal = spacing.windowPadding)
                            .padding(top = spacing.largeAdaptive),
                        thickness = Dp.Hairline,
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.windowPadding + 18.dp, vertical = spacing.mediumAdaptive),
                        verticalArrangement = Arrangement.spacedBy(spacing.smallAdaptive)
                    ) {
                        Text(buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "https://geoshare.ooooo.page/terms/",
                                    TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                                )
                            ) {
                                append(stringResource(R.string.billing_terms_of_service))
                            }
                        })
                        Text(buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "mailto:geoshare-support@jakubvalenta.cz",
                                    TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                                )
                            ) {
                                append(stringResource(R.string.billing_support_email))
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun BillingSupportingPane(
    billingOffers: List<Offer>,
    billingRefundableDuration: Duration,
    billingStatus: BillingStatus,
    innerPadding: PaddingValues,
    bottomCorners: Boolean,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
    onManageBillingProduct: (product: BillingProduct) -> Unit,
) {
    val spacing = LocalSpacing.current

    val sortedBillingOffers = remember(billingOffers) {
        billingOffers.sortedBy {
            when (it.period) {
                Offer.Period.ONE_TIME -> 1
                Offer.Period.MONTHLY -> 0
            }
        }
    }
    var selectedOffer by remember(sortedBillingOffers) { mutableStateOf(sortedBillingOffers.firstOrNull()) }

    when (billingStatus) {
        is BillingStatus.NotPurchased -> {
            ScaffoldAction(
                text = stringResource(R.string.billing_purchase_button),
                modifier = Modifier.testTag("geoShareBillingPurchaseButton"),
                innerPadding = innerPadding,
                bottomCorners = bottomCorners,
                onClick = {
                    selectedOffer?.let { selectedOffer ->
                        onLaunchBillingFlow(selectedOffer.token)
                    }
                },
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
                ElevatedCard(
                    Modifier
                        .selectableGroup()
                        .padding(horizontal = spacing.small),
                ) {
                    sortedBillingOffers.forEachIndexed { i, offer ->
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
                                .testTag(
                                    when (offer.period) {
                                        Offer.Period.ONE_TIME -> "geoShareBillingOfferOneTime"
                                        Offer.Period.MONTHLY -> "geoShareBillingOfferMonthly"
                                    },
                                )
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
            }
        }

        is BillingStatus.Purchased -> {
            when (billingStatus.product.type) {
                BillingProduct.Type.DONATION -> {}

                BillingProduct.Type.ONE_TIME -> {
                    ScaffoldAction(
                        text = stringResource(R.string.billing_order_history),
                        onClick = {
                            onManageBillingProduct(billingStatus.product)
                        },
                        innerPadding = innerPadding,
                        bottomCorners = bottomCorners,
                        modifier = Modifier.testTag("geoShareBillingManageButtonOneTime"),
                    ) {
                        if (billingStatus.refundable) {
                            Text(
                                stringResource(
                                    R.string.billing_refund_description,
                                    billingRefundableDuration.toInt(DurationUnit.HOURS)
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                BillingProduct.Type.SUBSCRIPTION -> {
                    ScaffoldAction(
                        text = stringResource(R.string.billing_manage_subscription),
                        onClick = {
                            onManageBillingProduct(billingStatus.product)
                        },
                        innerPadding = innerPadding,
                        bottomCorners = bottomCorners,
                        modifier = Modifier.testTag("geoShareBillingManageButtonSubscription"),
                    ) {
                        Text(
                            stringResource(R.string.billing_manage_subscription_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedDonationPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedDonationPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedDonationPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedOneTimePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedOneTimePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedOneTimePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedOneTimeNotRefundablePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                false,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedOneTimeNotRefundablePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                false,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedOneTimeNotRefundablePreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                false,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedSubscriptionPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedSubscriptionPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedSubscriptionPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = Message(
                stringResource(
                    R.string.billing_purchase_success, stringResource(R.string.app_name_pro)
                )
            ),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkSuccessPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = Message(
                stringResource(
                    R.string.billing_purchase_success, stringResource(R.string.app_name_pro)
                )
            ),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletSuccessPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = Message(
                stringResource(
                    R.string.billing_purchase_success, stringResource(R.string.app_name_pro)
                )
            ),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = Message(stringResource(R.string.billing_purchase_error_cancelled), isError = true),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkErrorPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = Message(stringResource(R.string.billing_purchase_error_cancelled), isError = true),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletErrorPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = Message(stringResource(R.string.billing_purchase_error_cancelled), isError = true),
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                true,
            ),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            onBack = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}
