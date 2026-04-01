package page.ooooo.geoshare.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Message
import page.ooooo.geoshare.lib.billing.AutomationFeature
import page.ooooo.geoshare.lib.billing.BillingOffers
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.FakeOneTimeOffer
import page.ooooo.geoshare.lib.billing.FakeSubscriptionOffer
import page.ooooo.geoshare.lib.billing.Feature
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.ui.components.AnimatedMessage
import page.ooooo.geoshare.ui.components.BasicSupportingPaneScaffold
import page.ooooo.geoshare.ui.components.LargeButton
import page.ooooo.geoshare.ui.components.MainHeadline
import page.ooooo.geoshare.ui.components.MessageSnackbarHost
import page.ooooo.geoshare.ui.components.MessageSnackbarVisuals
import page.ooooo.geoshare.ui.components.ScaffoldAction
import page.ooooo.geoshare.ui.components.SegmentedList
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
    billingViewModel: BillingViewModel,
) {
    val context = LocalContext.current
    val billingAppNameResId = billingViewModel.billingAppNameResId
    val billingFeatures = billingViewModel.billingFeatures
    val billingMessage by billingViewModel.billingMessage.collectAsStateWithLifecycle()
    val billingOffers by billingViewModel.billingOffers.collectAsStateWithLifecycle()
    val billingRefundableDuration = billingViewModel.billingRefundableDuration
    val billingStatus by billingViewModel.billingStatus.collectAsStateWithLifecycle()

    BillingScreen(
        billingAppNameResId = billingAppNameResId,
        billingFeatures = billingFeatures,
        billingMessage = billingMessage,
        billingOffers = billingOffers,
        billingRefundableDuration = billingRefundableDuration,
        billingStatus = billingStatus,
        onBack = onBack,
        onDismissMessage = { billingViewModel.dismissMessage() },
        onLaunchBillingFlow = { offerToken ->
            billingViewModel.launchBillingFlow(context as Activity, offerToken)
        },
        onManageBillingProduct = { product ->
            billingViewModel.manageBillingProduct(context as Activity, product)
        },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun BillingScreen(
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingMessage: Message?,
    billingOffers: BillingOffers,
    billingRefundableDuration: Duration,
    billingStatus: BillingStatus,
    animationsEnabled: Boolean = true,
    onBack: () -> Unit,
    onDismissMessage: () -> Unit,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
    onManageBillingProduct: (product: BillingProduct) -> Unit,
) {
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Message

    LaunchedEffect(billingMessage) {
        if (billingMessage != null) {
            snackbarHostState.showSnackbar(MessageSnackbarVisuals(billingMessage))
            onDismissMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            MessageSnackbarHost(snackbarHostState)
        },
    ) {
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
                        animationsEnabled,
                        billingAppNameResId,
                        billingFeatures,
                        billingOffers,
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BillingMainPane(
    animationsEnabled: Boolean,
    billingAppNameResId: Int,
    billingFeatures: List<Feature>,
    billingOffers: BillingOffers,
    billingStatus: BillingStatus,
) {
    val spacing = LocalSpacing.current

    AnimatedMessage(
        state = billingStatus,
        isMessageShown = { billingStatus ->
            billingStatus is BillingStatus.Purchased ||
                billingStatus is BillingStatus.NotPurchased && billingStatus.pending ||
                billingStatus is BillingStatus.NotPurchased && (billingOffers as? BillingOffers.Done)?.offers?.isEmpty() == true
        },
        animationsEnabled = animationsEnabled,
    ) { billingStatus ->
        when (billingStatus) {
            is BillingStatus.Purchased if !billingStatus.expired ->
                BillingStatusCard(
                    stringResource(R.string.billing_purchase_success, stringResource(R.string.app_name_pro)),
                    Modifier.testTag("geoShareBillingStatusPurchased"),
                )

            is BillingStatus.Purchased ->
                BillingStatusCard(
                    stringResource(R.string.billing_status_expired),
                    Modifier.testTag("geoShareBillingStatusExpired"),
                )

            is BillingStatus.NotPurchased if billingStatus.pending ->
                BillingStatusCard(
                    stringResource(R.string.billing_status_pending),
                    Modifier.testTag("geoShareBillingStatusPending"),
                )

            is BillingStatus.NotPurchased if (billingOffers as? BillingOffers.Done)?.offers?.isEmpty() == true ->
                BillingStatusCard(
                    stringResource(
                        R.string.billing_offers_empty,
                        stringResource(R.string.app_name_pro),
                    ),
                )

            else -> {}
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
                    }
                ),
                Modifier.padding(top = spacing.mediumAdaptive),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            MainHeadline(billingAppNameResId, Modifier.padding(top = spacing.smallAdaptive), iconEnabled = false)
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
        }
    }
}

@Composable
private fun BillingStatusCard(text: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.windowPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Text(
            text,
            modifier.padding(spacing.small),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun BillingLegalText() {
    Text(
        buildAnnotatedString {
            withLink(
                LinkAnnotation.Url(
                    "https://geoshare.ooooo.page/terms/",
                    TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                )
            ) {
                append(stringResource(R.string.billing_terms_of_service))
            }
            append(" • ")
            withLink(
                LinkAnnotation.Url(
                    "mailto:geoshare-support@jakubvalenta.cz",
                    TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
                )
            ) {
                append(stringResource(R.string.billing_support_email))
            }
        },
        Modifier
            .fillMaxWidth()
            .padding(horizontal = LocalSpacing.current.windowPadding),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BillingSupportingPane(
    billingOffers: BillingOffers,
    billingRefundableDuration: Duration,
    billingStatus: BillingStatus,
    innerPadding: PaddingValues,
    bottomCorners: Boolean,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
    onManageBillingProduct: (product: BillingProduct) -> Unit,
) {
    val spacing = LocalSpacing.current

    val sortedBillingOffers = remember(billingOffers) {
        (billingOffers as? BillingOffers.Done)?.offers?.sortedBy {
            when (it.period) {
                Offer.Period.ONE_TIME -> 1
                Offer.Period.MONTHLY -> 0
            }
        } ?: emptyList()
    }
    var selectedOffer by remember(sortedBillingOffers) { mutableStateOf(sortedBillingOffers.firstOrNull()) }

    when (billingStatus) {
        is BillingStatus.NotPurchased if sortedBillingOffers.isNotEmpty() -> {
            ScaffoldAction(
                innerPadding = innerPadding,
                bottomCorners = bottomCorners,
            ) {
                SegmentedList(
                    values = sortedBillingOffers,
                    modifier = Modifier.padding(horizontal = spacing.small),
                    itemHeadline = { offer ->
                        when (offer.period) {
                            Offer.Period.ONE_TIME -> stringResource(R.string.billing_one_time)
                            Offer.Period.MONTHLY -> stringResource(R.string.billing_subscription_monthly)
                        }
                    },
                    itemIsSelected = { offer -> offer == selectedOffer },
                    itemOnClick = { offer -> selectedOffer = offer },
                    itemLeadingContent = { offer ->
                        {
                            RadioButton(
                                selected = offer == selectedOffer,
                                // Null recommended for accessibility with screen readers
                                onClick = null,
                            )
                        }
                    },
                    itemSupportingContent = { offer ->
                        {
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
                    },
                    itemTrailingContent = { offer ->
                        {
                            Text(
                                offer.formattedPrice,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                    itemTestTag = { offer -> "geoShareBillingOffer_${offer.period}" },
                )
                LargeButton(
                    stringResource(R.string.billing_purchase_button),
                    Modifier.testTag("geoShareBillingPurchaseButton"),
                    enabled = selectedOffer != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    selectedOffer?.let { selectedOffer ->
                        onLaunchBillingFlow(selectedOffer.token)
                    }
                }
                BillingLegalText()
            }
        }

        is BillingStatus.NotPurchased -> {}

        is BillingStatus.Purchased -> {
            when (billingStatus.product.type) {
                BillingProduct.Type.DONATION -> {}

                BillingProduct.Type.ONE_TIME -> {
                    ScaffoldAction(
                        innerPadding = innerPadding,
                        bottomCorners = bottomCorners,
                    ) {
                        Text(
                            stringResource(
                                R.string.billing_refund_description,
                                billingRefundableDuration.toInt(DurationUnit.HOURS)
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LargeButton(
                            stringResource(R.string.billing_order_history),
                            Modifier.testTag("geoShareBillingManageButtonOneTime"),
                        ) {
                            onManageBillingProduct(billingStatus.product)
                        }
                        BillingLegalText()
                    }
                }

                BillingProduct.Type.SUBSCRIPTION -> {
                    ScaffoldAction(
                        innerPadding = innerPadding,
                        bottomCorners = bottomCorners,
                    ) {
                        Text(
                            stringResource(R.string.billing_manage_subscription_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LargeButton(
                            stringResource(R.string.billing_manage_subscription),
                            Modifier.testTag("geoShareBillingManageButtonSubscription"),
                        ) {
                            onManageBillingProduct(billingStatus.product)
                        }
                        BillingLegalText()
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PendingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = true),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPendingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = true),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPendingPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = true),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.DONATION),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.ONE_TIME),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                expired = false,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedSubscriptionExpiredPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                expired = true,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedSubscriptionExpiredPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                expired = true,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedSubscriptionExpiredPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf(FakeSubscriptionOffer, FakeOneTimeOffer)),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Purchased(
                BillingProduct("test", BillingProduct.Type.SUBSCRIPTION),
                expired = true,
                refundable = true,
            ),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf()),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf()),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletEmptyPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Done(persistentListOf()),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Loading(),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Loading(),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
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
            billingOffers = BillingOffers.Loading(),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.Loading(),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingOffersPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Loading(),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLoadingOffersPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Loading(),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletLoadingOffersPreview() {
    AppTheme {
        BillingScreen(
            billingAppNameResId = R.string.app_name_pro,
            billingFeatures = listOf(AutomationFeature),
            billingMessage = null,
            billingOffers = BillingOffers.Loading(),
            billingRefundableDuration = 48.hours,
            billingStatus = BillingStatus.NotPurchased(pending = false),
            animationsEnabled = false,
            onBack = {},
            onDismissMessage = {},
            onLaunchBillingFlow = {},
            onManageBillingProduct = {},
        )
    }
}
