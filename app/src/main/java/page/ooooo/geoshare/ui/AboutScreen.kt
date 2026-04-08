package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.ui.components.BasicSupportingPaneScaffold
import page.ooooo.geoshare.ui.components.LargeButton
import page.ooooo.geoshare.ui.components.ParagraphHtml
import page.ooooo.geoshare.ui.components.ParagraphText
import page.ooooo.geoshare.ui.components.ScaffoldAction
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    onNavigateToLicensesScreen: () -> Unit = {},
    billingViewModel: BillingViewModel,
) {
    val billingStatus by billingViewModel.billingStatus.collectAsStateWithLifecycle()
    val donationVisible = remember(billingStatus) {
        (billingStatus as? BillingStatus.Purchased)?.product?.type == BillingProduct.Type.DONATION
    }

    AboutScreen(
        donationVisible = donationVisible,
        onBack = onBack,
        onNavigateToLicensesScreen = onNavigateToLicensesScreen,
    )
}

@Composable
private fun AboutScreen(
    donationVisible: Boolean,
    onBack: () -> Unit = {},
    onNavigateToLicensesScreen: () -> Unit = {},
) {
    val spacing = LocalSpacing.current

    BasicSupportingPaneScaffold(
        title = { Text(stringResource(R.string.about_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back_content_description),
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
                    .padding(horizontal = spacing.windowPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                AboutMainPane(
                    donationVisible = donationVisible,
                    onNavigateToLicensesScreen = onNavigateToLicensesScreen,
                )
            }
            if (!wide) {
                AboutSupportingPane(
                    donationVisible = donationVisible,
                    innerPadding = innerPadding,
                    bottomCorners = false,
                )
            }
        },
        supportingPane = {
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                AboutSupportingPane(
                    donationVisible = donationVisible,
                    innerPadding = PaddingValues.Zero,
                    bottomCorners = true,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
private fun ColumnScope.AboutMainPane(
    donationVisible: Boolean,
    onNavigateToLicensesScreen: () -> Unit,
) {
    val spacing = LocalSpacing.current

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
        stringResource(R.string.about_app_name_and_version, appName, BuildConfig.VERSION_NAME),
        Modifier.padding(bottom = spacing.mediumAdaptive),
        style = MaterialTheme.typography.headlineSmall,
    )
    ParagraphHtml(
        stringResource(
            R.string.about_text,
            appName,
            stringResource(R.string.about_support_email),
        )
    )
    ParagraphText(
        buildAnnotatedString {
            withLink(
                LinkAnnotation.Clickable(
                    "licenses",
                    styles = TextLinkStyles(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                ) {
                    onNavigateToLicensesScreen()
                }
            ) {
                append(stringResource(R.string.licenses))
            }
        }
    )

    if (donationVisible) {
        ElevatedCard(
            modifier = Modifier.padding(top = spacing.largeAdaptive),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
        ) {
            ParagraphText(
                stringResource(R.string.about_text_google_play),
                Modifier.padding(spacing.small),
            )
        }
    }
}

@Composable
private fun AboutSupportingPane(
    donationVisible: Boolean,
    innerPadding: PaddingValues,
    bottomCorners: Boolean,
) {
    if (donationVisible) {
        val uriHandler = LocalUriHandler.current

        ScaffoldAction(
            innerPadding = innerPadding,
            bottomCorners = bottomCorners,
        ) {
            Text(
                stringResource(R.string.donation_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            LargeButton(
                stringResource(R.string.donation_button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                uriHandler.openUri("https://ko-fi.com/jakubvalenta")
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        AboutScreen(donationVisible = false)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        AboutScreen(donationVisible = false)
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        AboutScreen(donationVisible = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun DonationPreview() {
    AppTheme {
        AboutScreen(donationVisible = true)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDonationPreview() {
    AppTheme {
        AboutScreen(donationVisible = true)
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletDonationPreview() {
    AppTheme {
        AboutScreen(donationVisible = true)
    }
}
