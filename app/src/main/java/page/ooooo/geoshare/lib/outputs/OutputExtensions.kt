package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

fun List<Output>.getText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
    this.firstNotNullOfOrNull { (it as? Output.Text?)?.getText(position, uriQuote) } ?: ""

fun List<Output>.getSupportingText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String =
    this.firstNotNullOfOrNull { (it as? Output.SupportingText?)?.getText(position, uriQuote) } ?: ""

fun List<Output>.getActions(): List<Output.Action> =
    this.mapNotNull { it as? Output.Action }

fun List<Output>.getAppActions(): List<Output.AppAction> =
    this.mapNotNull { it as? Output.AppAction }

fun List<Output>.getPointActions(): List<Output.PointAction> =
    this.mapNotNull { it as? Output.PointAction }

fun List<Output>.getChips(): List<Output.Chip> =
    this.mapNotNull { it as? Output.Chip }

fun List<Output>.getFirstOpenChooserAction(): Output.OpenChooserAction? =
    this.firstNotNullOfOrNull { it as? Output.OpenChooserAction }

fun List<Output>.getFirstSaveGpxAction(): Output.SaveGpxAction? =
    this.firstNotNullOfOrNull { it as? Output.SaveGpxAction }

fun List<Output.PointAction>.getPointText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String =
    this.firstNotNullOfOrNull { (it as? Output.PointText)?.getText(point, uriQuote) } ?: ""
