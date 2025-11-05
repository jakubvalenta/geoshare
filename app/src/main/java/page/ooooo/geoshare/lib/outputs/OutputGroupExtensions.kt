package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

fun <T> List<OutputGroup<T>>.getTextOutput() =
    this.firstNotNullOfOrNull { it.getTextOutput() }

fun <T> List<OutputGroup<T>>.getSupportingTextOutput() =
    this.firstNotNullOfOrNull { it.getSupportingTextOutput() }

fun <T> List<OutputGroup<T>>.getAppOutputs(packageNames: List<String>) =
    this.flatMap { it.getAppOutputs(packageNames) }

fun <T> List<OutputGroup<T>>.getChipOutputs() =
    this.flatMap { it.getChipOutputs() }

fun <T> List<OutputGroup<T>>.getActionOutputs() =
    this.flatMap { it.getActionOutputs() }

fun <T> List<OutputGroup<T>>.getChooserOutput() =
    this.firstNotNullOfOrNull { it.getChooserOutput() }

fun <T> List<OutputGroup<T>>.getAutomations(packageNames: List<String>) =
    this.flatMap { it.getAutomations(packageNames) }

fun <T> List<OutputGroup<T>>.findAutomation(type: Automation.Type, packageName: String?) =
    this.firstNotNullOfOrNull { it.findAutomation(type, packageName) }

fun List<OutputGroup<Position>>.genRandomUriString(uriQuote: UriQuote = DefaultUriQuote()): String? =
    this.getActionOutputs()
        .mapNotNull { (it.getAction(Position.genRandomPosition(), uriQuote) as? Action.Copy)?.text }
        .randomOrNull()
