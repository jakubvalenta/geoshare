package page.ooooo.geoshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import page.ooooo.geoshare.BuildConfig
import page.ooooo.geoshare.ConversionViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.billing.BillingProduct
import page.ooooo.geoshare.lib.billing.BillingStatus
import page.ooooo.geoshare.ui.components.BasicScaffold
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
    val donation = remember(billingStatus) {
        (billingStatus as? BillingStatus.Purchased)?.product?.type == BillingProduct.Type.DONATION
    }

    AboutScreen(
        donation = donation,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AboutScreen(
    donation: Boolean,
    onBack: () -> Unit = {},
) {
    val spacing = LocalSpacing.current

    BasicScaffold(
        title = { Text(stringResource(R.string.about_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back_content_description)
                )
            }
        },
        mainPane = { innerPadding, wide ->
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                AboutMainPane(donation = donation)
            }
            if (!wide) {
                Spacer(Modifier.weight(1f))
                AboutSupportingPane(donation = donation, innerPadding = innerPadding, bottomCorners = false)
            }
        },
        supportingPane = {
            Column(Modifier.padding(horizontal = spacing.windowPadding)) {
                AboutSupportingPane(donation = donation, innerPadding = PaddingValues.Zero, bottomCorners = true)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
private fun ColumnScope.AboutMainPane(
    donation: Boolean,
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
    ParagraphHtml(stringResource(R.string.about_text, appName))

    if (donation) {
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

@Composable
private fun AboutSupportingPane(
    donation: Boolean,
    innerPadding: PaddingValues,
    bottomCorners: Boolean,
) {
    if (donation) {
        val uriHandler = LocalUriHandler.current

        ScaffoldAction(
            text = stringResource(R.string.donation_button),
            onClick = {
                uriHandler.openUri("https://ko-fi.com/jakubvalenta")
            },
            innerPadding = innerPadding,
            bottomCorners = bottomCorners,
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

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        AboutScreen(donation = false)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        AboutScreen(donation = false)
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletPreview() {
    AppTheme {
        AboutScreen(donation = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun DonationPreview() {
    AppTheme {
        AboutScreen(donation = true)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkDonationPreview() {
    AppTheme {
        AboutScreen(donation = true)
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun TabletDonationPreview() {
    AppTheme {
        AboutScreen(donation = true)
    }
}
