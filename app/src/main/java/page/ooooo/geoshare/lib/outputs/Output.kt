package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.point.Point

interface Output {
    fun getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote): String? = null

    fun getPointsActions(): List<Action> = emptyList()

    fun getPointActions(): List<Action> = emptyList()

    fun getAppActions(apps: List<AndroidTools.App>): List<Pair<String, Action>> = emptyList()

    fun getAllPointsChipActions(): List<Action> = emptyList()

    fun getLastPointChipActions(): List<Action> = emptyList()

    fun getChooserAction(): OpenChooserAction? = null

    fun getRandomAction(): CopyAction? = null

    fun getAutomations(apps: List<AndroidTools.App>): List<Automation> = emptyList()

    fun findAutomation(type: Automation.Type, packageName: String?): Automation? = null
}
