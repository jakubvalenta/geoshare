package page.ooooo.geoshare.lib.outputs

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object CoordinatesOutput : Output {

    object CopyCoordsDecAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_COORDS_DEC
        override val packageName = ""
        override val testTag = "geoShareUserPreferenceAutomationCopyCoordsDec"

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(position.toCoordsDecString())

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_coordinates_in_format,
                    Position.example.toCoordsDecString(),
                )
            )
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    object CopyCoordsDegMinSecAutomation : Automation.HasSuccessMessage {
        override val type = Automation.Type.COPY_COORDS_NSWE_DEC
        override val packageName = ""
        override val testTag = null

        override fun getAction(position: Position, uriQuote: UriQuote) =
            Action.Copy(position.toDegMinSecCoordsString())

        @Composable
        override fun Label() {
            Text(
                stringResource(
                    R.string.conversion_succeeded_copy_coordinates_in_format,
                    Position.example.toDegMinSecCoordsString(),
                )
            )
        }

        @Composable
        override fun successText() = stringResource(R.string.conversion_automation_copy_succeeded)
    }

    override fun getText(position: Position, uriQuote: UriQuote) = position.toDegMinSecCoordsString()

    override fun getText(point: Point, uriQuote: UriQuote) = point.toDegMinSecCoordsString()

    override fun getActions(position: Position, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.Copy(position.toDegMinSecCoordsString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.Item(Action.Copy(position.toCoordsDecString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
    )

    override fun getActions(point: Point, uriQuote: UriQuote) = listOf<Output.Item<Action>>(
        Output.Item(Action.Copy(point.toDegMinSecCoordsString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
        Output.Item(Action.Copy(point.toCoordsDecString())) {
            stringResource(R.string.conversion_succeeded_copy_coordinates)
        },
    )

    override fun getAutomations(context: Context): List<Automation> = listOf(
        CopyCoordsDecAutomation,
        CopyCoordsDegMinSecAutomation,
    )

    override fun findAutomation(type: Automation.Type, packageName: String?) =
        when (type) {
            Automation.Type.COPY_COORDS_DEC -> CopyCoordsDecAutomation
            Automation.Type.COPY_COORDS_NSWE_DEC -> CopyCoordsDegMinSecAutomation
            else -> null
        }

    override fun getChips(position: Position, uriQuote: UriQuote) = emptyList<Output.Item<Action>>()
}
