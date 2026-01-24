package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position

interface Output {
    fun getText(position: Position, i: Int?, uriQuote: UriQuote): String? = null

    @Composable
    fun getName(position: Position, i: Int?, uriQuote: UriQuote): String? = null

    fun getPositionActions(): List<Action> = emptyList()

    fun getPointActions(): List<Action> = emptyList()

    fun getAppActions(apps: List<AndroidTools.App>): List<Pair<String, Action>> = emptyList()

    fun getChipActions(): List<Action> = emptyList()

    fun getChooserAction(): OpenChooserAction? = null

    fun getRandomAction(): CopyAction? = null

    fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = emptyList()

    fun findAutomation(type: Automation.Type, packageName: String?): Automation? = null
}
