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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.inputs.Input
import page.ooooo.geoshare.lib.point.Point
import page.ooooo.geoshare.ui.InputViewModel
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun MainFormLinks(
    modifier: Modifier = Modifier,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
    inputViewModel: InputViewModel = hiltViewModel(),
) {
    val allInputs = inputViewModel.allInputs

    MainFormLinks(
        allInputs = allInputs,
        modifier = modifier,
        onNavigateToInputsScreen = onNavigateToInputsScreen,
        onNavigateToIntroScreen = onNavigateToIntroScreen,
        onSetErrorMessageResId = onSetErrorMessageResId,
        onUpdateInput = onUpdateInput,
    )
}

@Composable
private fun MainFormLinks(
    allInputs: List<Input>,
    modifier: Modifier = Modifier,
    onNavigateToInputsScreen: () -> Unit,
    onNavigateToIntroScreen: () -> Unit,
    onSetErrorMessageResId: (newErrorMessageResId: Int?) -> Unit,
    onUpdateInput: (newInputUriString: String) -> Unit,
) {
    val resources = LocalResources.current
    val spacing = LocalSpacing.current

    Column(
        modifier
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
            val randomPoint = Point.genRandomPoint(
                name = resources.getString(R.string.intro_how_to_share_google_maps_screenshot_place),
            )
            allInputs
                .shuffled()
                .firstNotNullOfOrNull { (it as? Input.HasRandomUri)?.genRandomUri(randomPoint) }
                ?.let { newInputUriString ->
                    onUpdateInput(newInputUriString)
                    onSetErrorMessageResId(null)
                }
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
            MainFormLinks(
                allInputs = emptyList(),
                onNavigateToInputsScreen = {},
                onNavigateToIntroScreen = {},
                onSetErrorMessageResId = {},
                onUpdateInput = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            MainFormLinks(
                allInputs = emptyList(),
                onNavigateToInputsScreen = {},
                onNavigateToIntroScreen = {},
                onSetErrorMessageResId = {},
                onUpdateInput = {},
            )
        }
    }
}
