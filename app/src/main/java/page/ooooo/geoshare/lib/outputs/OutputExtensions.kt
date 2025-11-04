package page.ooooo.geoshare.lib.outputs

import page.ooooo.geoshare.lib.Action

fun List<Output>.getText(): String = this.firstNotNullOfOrNull { (it as? Output.Text?)?.text } ?: ""

fun List<Output>.getSupportingText(): String = this.firstNotNullOfOrNull { (it as? Output.SupportingText?)?.text } ?: ""

fun List<Output>.getActions(): List<Output.Action> = this.mapNotNull { it as? Output.Action }

fun List<Output>.getAppActions(): List<Output.AppAction> = this.mapNotNull { it as? Output.AppAction }

fun List<Output>.getPointActions(): List<Output.PointAction> = this.mapNotNull { it as? Output.PointAction }

fun List<Output>.getChips(): List<Output.Chip> = this.mapNotNull { it as? Output.Chip }

fun List<Output.PointAction>.getPointText(): String =
    this.firstNotNullOfOrNull { (_, action) -> (action as? Action.Copy?)?.text } ?: ""
