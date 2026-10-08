package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.WGS84Point

/**
 * @see DebugUriInput
 */
object DebugWebViewInput : WebViewInput {
    override val group = InputGroup.DEBUG
    override fun getName(resources: Resources) = "Debug Input (WebView)"

    // language=JavaScript
    override fun getUnsafeExtractionJavaScript() = """
        () => location.href;
    """.trimIndent()

    override fun parse(data: String, match: String, resources: Resources) =
        parseResult {
            points = persistentListOf(WGS84Point(NaivePoint.genRandomPoint()))
        }
}
