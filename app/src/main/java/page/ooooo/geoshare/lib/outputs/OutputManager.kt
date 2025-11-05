package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Automation

interface OutputManager {
    fun getOutputs(packageNames: List<String>): List<Output>
    fun getAutomations(packageNames: List<String>): List<Automation>
    fun findAutomation(type: Automation.Type, packageName: String?): Automation?
}
