package page.ooooo.geoshare.ui

import android.app.Activity
import android.content.res.Configuration
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.lib.billing.FakeFullPlan
import page.ooooo.geoshare.lib.billing.FakeOneTimeOffer
import page.ooooo.geoshare.lib.billing.FakeSubscriptionOffer
import page.ooooo.geoshare.lib.billing.Offer
import page.ooooo.geoshare.lib.billing.Plan
import page.ooooo.geoshare.ui.components.AppHeadline
import page.ooooo.geoshare.ui.components.LargeButton
import page.ooooo.geoshare.ui.components.TextList
import page.ooooo.geoshare.ui.components.TextListBullet
import page.ooooo.geoshare.ui.components.TextListItem
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun BillingScreen(
    onBack: () -> Unit = {},
    viewModel: ConversionViewModel,
) {
    val context = LocalContext.current
    val errorMessageResId by viewModel.billingErrorMessageResId.collectAsStateWithLifecycle()
    val billingStatus by viewModel.billingStatus.collectAsStateWithLifecycle()
    val offers by viewModel.billingOffers.collectAsStateWithLifecycle()

    BillingScreen(
        billingStatus = billingStatus,
        errorMessageResId = errorMessageResId,
        offers = offers,
        onBack = onBack,
        onLaunchBillingFlow = { offerToken ->
            viewModel.launchBillingFlow(context as Activity, offerToken)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillingScreen(
    billingStatus: BillingStatus,
    @StringRes errorMessageResId: Int?,
    offers: List<Offer>,
    onBack: () -> Unit,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalSpacing.current
    val expanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val plan = (billingStatus as? BillingStatus.Done)?.plan

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
                        BillingFirstPane(plan)
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
                            errorMessageResId = errorMessageResId,
                            offers = offers,
                            onLaunchBillingFlow = onLaunchBillingFlow,
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
                    BillingFirstPane(plan)
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
                        errorMessageResId = errorMessageResId,
                        offers = offers,
                        onLaunchBillingFlow = onLaunchBillingFlow,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BillingFirstPane(plan: Plan?) {
    val spacing = LocalSpacing.current

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            Modifier.padding(top = spacing.largeAdaptive),
            shape = MaterialShapes.Sunny.toShape(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Icon(
                painterResource(R.drawable.crown_24px),
                contentDescription = stringResource(R.string.billing_pro_feature),
                Modifier
                    .padding(15.dp)
                    .requiredSize(48.dp),
            )
        }
        Text(
            stringResource(R.string.billing_intro),
            Modifier.padding(top = spacing.mediumAdaptive),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        AppHeadline(plan, Modifier.padding(top = spacing.smallAdaptive), iconEnabled = false)
    }
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
        TextList(
            Modifier.padding(
                start = spacing.small,
                top = spacing.largeAdaptive,
                end = spacing.medium,
                bottom = spacing.mediumAdaptive,
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
                    stringResource(R.string.billing_automation_title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            TextListItem(Modifier.padding(bottom = spacing.smallAdaptive)) {
                Text(
                    stringResource(R.string.user_preferences_automation_description),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineBreak = LineBreak.Paragraph,
                        hyphens = Hyphens.Auto,
                    )
                )
            }
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_open_app))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_navigate))
                }
                TextListItem(Modifier.padding(bottom = 2.dp), bullet = { TextListBullet() }) {
                    Text(stringResource(R.string.billing_automation_copy))
                }
            }
        }
    }
    // TODO Show success message
    // TODO Change content if subscribed
    // TODO Manage your subscription: https://play.google.com/store/account/subscriptions?sku=your-sub-product-id&package=your-app-package
    // TODO Show subscription error: https://developer.android.com/google/play/billing/subscriptions#in-app-messaging
    // TODO Add TOS link
}

@Composable
private fun BillingSecondPane(
    @StringRes errorMessageResId: Int?,
    offers: List<Offer>,
    onLaunchBillingFlow: (offerToken: String) -> Unit,
) {
    val spacing = LocalSpacing.current
    var selectedOffer by remember { mutableStateOf(offers[0]) }

    Column(
        Modifier.safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessageResId != null) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(start = spacing.small, top = spacing.small, end = spacing.small),
                shape = MaterialTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text(
                    stringResource(errorMessageResId),
                    Modifier.padding(spacing.small),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        ElevatedCard(
            Modifier
                .selectableGroup()
                .padding(spacing.small),
        ) {
            offers.forEachIndexed { i, offer ->
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
                                        Offer.Period.ONE_TIME -> stringResource(R.string.billing_offer_one_time_title)
                                        Offer.Period.MONTHLY -> stringResource(R.string.billing_offer_subscription_title)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    when (offer.period) {
                                        Offer.Period.ONE_TIME -> stringResource(R.string.billing_offer_one_time_description)
                                        Offer.Period.MONTHLY -> stringResource(R.string.billing_offer_subscription_description)
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
                if (i < offers.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        LargeButton(
            stringResource(R.string.billing_buy),
            Modifier.padding(vertical = spacing.small),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            onLaunchBillingFlow(selectedOffer.token)
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        BillingScreen(
            billingStatus = BillingStatus.Done(null),
            errorMessageResId = null,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
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
            billingStatus = BillingStatus.Done(null),
            errorMessageResId = null,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
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
            billingStatus = BillingStatus.Done(null),
            errorMessageResId = null,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PurchasedPreview() {
    AppTheme {
        BillingScreen(
            billingStatus = BillingStatus.Done(FakeFullPlan),
            errorMessageResId = null,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPurchasedPreview() {
    AppTheme {
        BillingScreen(
            billingStatus = BillingStatus.Done(FakeFullPlan),
            errorMessageResId = null,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPurchasedPreview() {
    AppTheme {
        BillingScreen(
            billingStatus = BillingStatus.Done(FakeFullPlan),
            errorMessageResId = null,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
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
            billingStatus = BillingStatus.Done(FakeFullPlan),
            errorMessageResId = R.string.billing_purchase_error_cancelled,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
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
            billingStatus = BillingStatus.Done(FakeFullPlan),
            errorMessageResId = R.string.billing_purchase_error_cancelled,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
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
            billingStatus = BillingStatus.Done(FakeFullPlan),
            errorMessageResId = R.string.billing_purchase_error_cancelled,
            offers = listOf(FakeSubscriptionOffer, FakeOneTimeOffer),
            onBack = {},
            onLaunchBillingFlow = {},
        )
    }
}
