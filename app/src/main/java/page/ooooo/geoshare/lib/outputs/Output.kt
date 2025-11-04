package page.ooooo.geoshare.lib.outputs

import android.content.Context
import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

interface Output {
    data class Item<T : Action>(val action: T, val label: @Composable () -> String)

    fun getText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?

    fun getText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String?

    fun getActions(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<Item<Action>>

    fun getActions(point: Point, uriQuote: UriQuote = DefaultUriQuote()): List<Item<Action>>

    fun getAutomations(context: Context): List<Automation>

    fun findAutomation(type: Automation.Type, packageName: String?): Automation?

    fun getChips(position: Position, uriQuote: UriQuote = DefaultUriQuote()): List<Item<Action>>
}
