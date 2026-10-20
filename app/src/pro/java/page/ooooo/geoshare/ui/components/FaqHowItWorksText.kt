package page.ooooo.geoshare.ui.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.ui.UserPreferenceGroupId
import page.ooooo.geoshare.ui.string.FormatArg
import page.ooooo.geoshare.ui.string.annotatedStringResource
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing
import page.ooooo.geoshare.ui.theme.tertiaryLinkStyles

@Composable
fun FaqHowItWorksText(
    onNavigateToUserPreferencesScreen: (groupId: UserPreferenceGroupId) -> Unit,
) {
    val appName = stringResource(R.string.app_name)
    val spacing = LocalSpacing.current

    ParagraphText(
        stringResource(R.string.faq_how_text_1, appName)
    )
    ParagraphText(
        stringResource(R.string.faq_how_text_2, appName)
    )
    TextList(verticalSpace = spacing.tiny) {
        FaqTextListItem(
            stringResource(R.string.faq_how_text_2_item_http),
            stringResource(R.string.faq_how_text_2_item_http_example),
        )
        FaqTextListItem(
            stringResource(R.string.faq_how_text_2_item_html),
            stringResource(R.string.faq_how_text_2_item_html_example),
        )
        FaqTextListItem(
            stringResource(R.string.faq_how_text_2_item_api),
            stringResource(R.string.faq_how_text_2_item_api_example),
        )
    }
    ParagraphText(
        stringResource(R.string.faq_how_text_3, appName)
    )
    ParagraphText(
        annotatedStringResource(
            R.string.faq_how_text_4,
            tertiaryLinkStyles(),
            FormatArg.Link(stringResource(R.string.faq_how_text_4_preferences_button)) {
                onNavigateToUserPreferencesScreen(UserPreferenceGroupId.CONNECTION_PERMISSION)
            },
        )
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            FaqHowItWorksText(onNavigateToUserPreferencesScreen = {})
        }
    }
}
