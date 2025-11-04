package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Automation
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

fun List<OutputManager>.getOutputs(
    position: Position,
    packageNames: List<String>,
    uriQuote: UriQuote = DefaultUriQuote(),
): List<Output> =
    this.flatMap { it.getOutputs(position, packageNames, uriQuote) }

fun List<OutputManager>.getAutomations(packageNames: List<String>): List<Automation> =
    this.flatMap { it.getAutomations(packageNames) }

fun List<OutputManager>.findAutomation(type: Automation.Type, packageName: String?): Automation? =
    this.firstNotNullOfOrNull { it.findAutomation(type, packageName) }

fun List<OutputManager>.genRandomUriString(uriQuote: UriQuote = DefaultUriQuote()): String? =
    this.randomOrNull()?.getOutputs(Position.genRandomPosition(), emptyList(), uriQuote)?.getText()
