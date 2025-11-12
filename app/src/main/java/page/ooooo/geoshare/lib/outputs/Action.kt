package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Immutable
import io.ktor.util.escapeHTML
import page.ooooo.geoshare.lib.conversion.ConversionRunContext
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.position.Position
import page.ooooo.geoshare.lib.position.Srs
import page.ooooo.geoshare.lib.UriQuote

sealed interface Action {
    suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext): Boolean

    @Immutable
    data class Copy(val text: String) : Action {
        override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
            intentTools.copyToClipboard(runContext.context, runContext.clipboard, text).let { true }
    }

    @Immutable
    data class OpenApp(val packageName: String, val uriString: String) : Action {
        override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
            intentTools.openApp(runContext.context, packageName, uriString)
    }

    @Immutable
    data class OpenChooser(val uriString: String) : Action {
        override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
            intentTools.openChooser(runContext.context, uriString)
    }

    @Immutable
    data class SaveGpx(val position: Position, val uriQuote: UriQuote = DefaultUriQuote()) : Action {
        override suspend fun run(intentTools: IntentTools, runContext: ConversionRunContext) =
            intentTools.launchSaveGpx(runContext.context, runContext.saveGpxLauncher)

        fun write(writer: Appendable) = writer.apply {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
            append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\"\n")
            append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
            append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")
            position.points?.map { point ->
                point.toStringPair(Srs.WGS84).let { (latStr, lonStr) ->
                    append("<wpt lat=\"$latStr\" lon=\"$lonStr\"")
                }
                if (point.desc != null) {
                    append(">\n")
                    append("    <desc>${point.desc.escapeHTML()}</desc>\n")
                    append("</wpt>\n")
                } else {
                    append(" />\n")
                }
            }
            append("</gpx>\n")
        }
    }
}
