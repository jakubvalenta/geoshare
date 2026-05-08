package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

object AppleMapsHtmlInput : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_apple_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_apple_maps_loading_indicator_title

    override suspend fun parse(data: ByteReadChannel, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
        buildParseResult {
            val latPattern = Regex("""<meta property="place:location:latitude" content="$LAT"""")
            val lonPattern = Regex("""<meta property="place:location:longitude" content="$LON"""")

            var lat: Double? = null
            var lon: Double? = null
            val name = prevPoints?.lastOrNull()?.name

            while (true) {
                val line = data.readLine() ?: break
                if (lat == null) {
                    latPattern.find(line)?.doubleGroupOrNull()?.let { lat = it }
                }
                if (lon == null) {
                    lonPattern.find(line)?.doubleGroupOrNull()?.let { lon = it }
                }
                if (lat != null && lon != null) {
                    points = persistentListOf(WGS84Point(lat, lon, name = name, source = Source.HTML))
                    return@buildParseResult
                }
            }
        }
}
