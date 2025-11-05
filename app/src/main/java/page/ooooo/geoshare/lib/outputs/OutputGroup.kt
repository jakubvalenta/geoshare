package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Action
import page.ooooo.geoshare.lib.Automation

interface OutputGroup<T> {
    fun getTextOutput(): Output.Text<T>?

    fun getSupportingTextOutput(): Output.Text<T>?

    fun getActionOutputs(): List<Output.Action<T, Action>>

    fun getAppOutputs(packageNames: List<String>): List<Output.App<T>>

    fun getChipOutputs(): List<Output.Action<T, Action>>

    fun getChooserOutput(): Output.Action<T, Action>?

    fun getAutomations(packageNames: List<String>): List<Automation>

    fun findAutomation(type: Automation.Type, packageName: String?): Automation?
}
