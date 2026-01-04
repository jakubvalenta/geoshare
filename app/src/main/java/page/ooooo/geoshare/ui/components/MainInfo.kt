package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.outputs.allOutputs
import page.ooooo.geoshare.lib.outputs.genRandomUriString
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainInfo(
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    Column(
        Modifier
            .padding(top = spacing.large)
            .padding(horizontal = spacing.windowPadding),
    ) {
        TextButton(onNavigateToInputsScreen) {
            Icon(
                Icons.Outlined.Info,
                null,
                Modifier.padding(end = spacing.tiny),
            )
            Text(stringResource(R.string.inputs_title))
        }
        TextButton(onNavigateToIntroScreen) {
            Icon(
                painterResource(R.drawable.rocket_launch_24px),
                null,
                Modifier.padding(end = spacing.tiny),
            )
            Text(stringResource(R.string.main_navigate_to_intro))
        }
        TextButton({
            allOutputs.genRandomUriString(
                resources.getString(R.string.intro_how_to_share_google_maps_screenshot_place),
            )?.let { uriString ->
                onUpdateInput(uriString)
            }
            onSetErrorMessageResId(null)
        }) {
            Icon(
                painterResource(R.drawable.ifl_24px),
                null,
                Modifier.padding(end = spacing.tiny),
            )
            Text(stringResource(R.string.main_random))
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            Column {
                MainInfo(
                    onNavigateToInputsScreen = {},
                    onNavigateToIntroScreen = {},
                    onSetErrorMessageResId = {},
                    onUpdateInput = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            Column {
                MainInfo(
                    onNavigateToInputsScreen = {},
                    onNavigateToIntroScreen = {},
                    onSetErrorMessageResId = {},
                    onUpdateInput = {},
                )
            }
        }
    }
}
