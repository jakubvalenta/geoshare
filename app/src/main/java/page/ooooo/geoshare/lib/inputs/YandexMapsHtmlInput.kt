package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Points
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point

object YandexMapsHtmlInput : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title

    override suspend fun parse(data: ByteReadChannel, prevPoints: Points?, uriQuote: UriQuote, log: ILog) =
        buildParseResult {
            val pointPattern = Regex("""pt=$LON%2C$LAT""")
            val namePattern = Regex("""itemProp="name"[^>]*>([^<]+)""")

            var naivePoint: NaivePoint? = null
            var name = prevPoints?.lastOrNull()?.name

            while (true) {
                val line = data.readLine() ?: break
                pointPattern.find(line)?.toLonLatPoint(Source.HTML)?.let {
                    naivePoint = it
                    continue
                }
                namePattern.find(line)?.groupOrNull()?.let {
                    name = it
                    break
                }
            }

            naivePoint?.also {
                points = persistentListOf(WGS84Point(it).copy(name = name))
            } ?: name?.also {
                points = persistentListOf(WGS84Point(name = name, source = Source.HTML))
            }
        }
}
