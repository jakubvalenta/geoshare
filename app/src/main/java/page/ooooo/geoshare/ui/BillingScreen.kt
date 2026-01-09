package page.ooooo.geoshare.ui

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.components.AppHeadline
import page.ooooo.geoshare.ui.components.LargeButton
import page.ooooo.geoshare.ui.components.TextList
import page.ooooo.geoshare.ui.components.TextListBullet
import page.ooooo.geoshare.ui.components.TextListItem
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

private interface PaymentOption {
    @Composable
    fun title(): String

    @Composable
    fun description(): String

    @Composable
    fun price(): String
}

private object SubscriptionPaymentOption : PaymentOption {
    @Composable
    override fun title() = stringResource(R.string.billing_product_subscription_title)

    @Composable
    override fun description() = stringResource(R.string.billing_product_subscription_description)

    @Composable
    override fun price() = """$1.5"""
}

private object OneTimePaymentOption : PaymentOption {
    @Composable
    override fun title() = stringResource(R.string.billing_product_one_time_title)

    @Composable
    override fun description() = stringResource(R.string.billing_product_one_time_description)

    @Composable
    override fun price() = """$19"""
}

// TODO Load payment options
private val paymentOptions = listOf(
    SubscriptionPaymentOption,
    OneTimePaymentOption,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onBack: () -> Unit = {},
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = LocalSpacing.current
    val expanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    var selectedPaymentOption by remember { mutableStateOf(paymentOptions[0]) }

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
                        BillingFirstPane()
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
                            selectedPaymentOption = selectedPaymentOption,
                            onSelectPaymentOption = { selectedPaymentOption = it },
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
                    BillingFirstPane()
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
                        selectedPaymentOption = selectedPaymentOption,
                        onSelectPaymentOption = { selectedPaymentOption = it },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BillingFirstPane() {
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
        AppHeadline(Modifier.padding(top = spacing.smallAdaptive), iconEnabled = false, paid = true)
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
    // TODO Change content if subscribed
    // TODO Add TOS link
}

@Composable
private fun BillingSecondPane(
    selectedPaymentOption: PaymentOption,
    onSelectPaymentOption: (paymentOption: PaymentOption) -> Unit,
) {
    val spacing = LocalSpacing.current

    Column(
        Modifier.safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            Modifier
                .selectableGroup()
                .padding(spacing.small),
        ) {
            paymentOptions.forEachIndexed { i, paymentOption ->
                ListItem(
                    headlineContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = paymentOption == selectedPaymentOption,
                                // Null recommended for accessibility with screen readers
                                onClick = null,
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    paymentOption.title(),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    paymentOption.description(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(
                                paymentOption.price(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                    modifier = Modifier
                        .selectable(
                            selected = paymentOption == selectedPaymentOption,
                            role = Role.RadioButton,
                            onClick = { onSelectPaymentOption(paymentOption) },
                        ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                )
                if (i < paymentOptions.size - 1) {
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
            // TODO
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        BillingScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        BillingScreen()
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        BillingScreen()
    }
}
