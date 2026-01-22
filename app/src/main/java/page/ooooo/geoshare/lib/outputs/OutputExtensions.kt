package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.position.Position

fun List<Output>.getText(position: Position, i: Int?, uriQuote: UriQuote = DefaultUriQuote()) =
    this.firstNotNullOfOrNull { it.getText(position, i, uriQuote) }

@Composable
fun List<Output>.getName(position: Position, i: Int?, uriQuote: UriQuote = DefaultUriQuote()) =
    this.firstNotNullOfOrNull { it.getName(position, i, uriQuote) }

fun List<Output>.getDescription(position: Position, uriQuote: UriQuote = DefaultUriQuote()) =
    this.firstNotNullOfOrNull { it.getDescription(position, uriQuote) }

fun List<Output>.getAppActions(apps: List<AndroidTools.App>) =
    this.flatMap { it.getAppActions(apps) }

fun List<Output>.getChipActions() =
    this.flatMap { it.getChipActions() }

fun List<Output>.getPositionActions() =
    this.flatMap { it.getPositionActions() }

fun List<Output>.getPointActions() =
    this.flatMap { it.getPointActions() }

fun List<Output>.getChooserAction() =
    this.firstNotNullOfOrNull { it.getChooserAction() }

fun List<Output>.getAutomations(apps: List<AndroidTools.App>) =
    this.flatMap { it.getAutomations(apps) }

fun List<Output>.findAutomation(type: Automation.Type, packageName: String?) =
    this.firstNotNullOfOrNull { it.findAutomation(type, packageName) }

fun List<Output>.genRandomUriString(name: String, uriQuote: UriQuote = DefaultUriQuote()): String? =
    Position.genRandomPosition(name = name).let { value ->
        this.mapNotNull { it.getRandomAction() }.randomOrNull()?.getText(value, null, uriQuote)
    }
