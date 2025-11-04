package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

interface OutputManager {
    fun getOutputs(position: Position, packageNames: List<String>, uriQuote: UriQuote = DefaultUriQuote()): List<Output>
    fun getAutomations(packageNames: List<String>): List<Automation>
    fun findAutomation(type: Automation.Type, packageName: String?): Automation?
}
