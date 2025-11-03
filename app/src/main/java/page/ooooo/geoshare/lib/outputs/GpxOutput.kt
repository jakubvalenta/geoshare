package page.ooooo.geoshare.lib.outputs

import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote

object GpxOutput : Output {
    override val packageNames = emptyList<String>()

    override fun getText(position: Position, uriQuote: UriQuote) = null

    override fun getText(point: Point, uriQuote: UriQuote) = null

    override fun getActions(position: Position, uriQuote: UriQuote) = getChips(position, uriQuote)

    override fun getActions(point: Point, uriQuote: UriQuote) = emptyList<Output.LabeledAction<Output.Action>>()

    override fun getChips(position: Position, uriQuote: UriQuote) = listOf<Output.LabeledAction<Output.Action>>(
        Output.LabeledAction(Output.Action.SaveGpx()) { // TODO Connect writer
            stringResource(R.string.conversion_succeeded_save_gpx)
        }
    )

    private fun writeGpx(position: Position, writer: Appendable, uriQuote: UriQuote = DefaultUriQuote()) =
        writer.apply {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
            append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\"\n")
            append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
            append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")
            position.points?.map { (lat, lon) ->
                append("<wpt lat=\"${uriQuote.encode(lat)}\" lon=\"${uriQuote.encode(lon)}\" />\n")
            }
            append("</gpx>\n")
        }
}
