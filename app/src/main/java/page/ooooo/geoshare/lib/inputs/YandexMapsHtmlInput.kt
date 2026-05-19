package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.client.engine.HttpClientEngine
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.geo.NaivePoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexMapsHtmlInput @Inject constructor(
    override val engine: HttpClientEngine,
    override val log: Log,
    override val uriQuote: UriQuote,
) : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_yandex_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_yandex_maps_loading_indicator_title

    override suspend fun parse(
        data: ByteReadChannel,
        match: String,
        prevResult: ParseResult?,
    ) = buildParseResult {
        val pointPattern = Regex("""pt=$LON%2C$LAT""")
        val namePattern = Regex("""itemProp="name"[^>]*>([^<]+)""")

        var naivePoint: NaivePoint? = null
        var name = prevResult?.points?.lastOrNull()?.name

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

    override fun toString() = "YandexMapsHtmlInput"
}
