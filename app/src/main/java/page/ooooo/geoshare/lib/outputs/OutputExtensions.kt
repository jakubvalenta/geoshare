package page.ooooo.geoshare.lib.outputs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.point.Point

fun List<Output>.getText(points: ImmutableList<Point>, i: Int?, uriQuote: UriQuote = DefaultUriQuote()) =
    this.firstNotNullOfOrNull { it.getText(points, i, uriQuote) }

fun List<Output>.getAppActions(apps: List<AndroidTools.App>) =
    this.flatMap { it.getAppActions(apps) }

fun List<Output>.getAllPointsChipActions() =
    this.flatMap { it.getAllPointsChipActions() }

fun List<Output>.getLastPointChipActions() =
    this.flatMap { it.getLastPointChipActions() }

fun List<Output>.getPointsActions() =
    this.flatMap { it.getPointsActions() }

fun List<Output>.getPointActions() =
    this.flatMap { it.getPointActions() }

fun List<Output>.getChooserAction() =
    this.firstNotNullOfOrNull { it.getChooserAction() }

fun List<Output>.getAutomations(apps: List<AndroidTools.App>) =
    this.flatMap { it.getAutomations(apps) }

fun List<Output>.findAutomation(type: Automation.Type, packageName: String?) =
    this.firstNotNullOfOrNull { it.findAutomation(type, packageName) }

fun List<Output>.genRandomUriString(name: String, uriQuote: UriQuote = DefaultUriQuote()): String? =
    persistentListOf(Point.genRandomPoint(name = name)).let { value ->
        this.mapNotNull { it.getRandomAction() }.randomOrNull()?.getText(value, null, uriQuote)
    }
