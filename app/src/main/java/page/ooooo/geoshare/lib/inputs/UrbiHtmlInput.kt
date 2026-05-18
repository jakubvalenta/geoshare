package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.decodeBasicHtmlEntities
import page.ooooo.geoshare.lib.extensions.doubleGroupOrNull
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import page.ooooo.geoshare.lib.extensions.toLonLatPoint
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import javax.inject.Singleton

@Singleton
class UrbiHtmlInput : BodyAsChannelInput {
    @StringRes
    override val permissionTitleResId = R.string.converter_urbi_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_urbi_loading_indicator_title

    override suspend fun parse(
        data: ByteReadChannel,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        val pattern = Regex("""property="twitter:image" content="([^"]+)""")

        // Notice that unlike in other Inputs, we don't copy any point names from prevResult here

        while (true) {
            val line = data.readLine() ?: break
            pattern.find(line)?.groupOrNull()?.let { attr ->
                val attrUri = Uri.parse(attr.decodeBasicHtmlEntities())
                attrUri.run {
                    // API map center
                    // https://share.api.2gis.ru/getimage?...&zoom={z}&center={lon},{lat}&title={name}...
                    LON_LAT_PATTERN.matchEntire(queryParams["center"])?.toLonLatPoint(Source.MAP_CENTER)?.let {
                        points = persistentListOf(
                            WGS84Point(it).copy(
                                z = Z_PATTERN.matchEntire(queryParams["zoom"])?.doubleGroupOrNull(),
                                name = Q_PARAM_PATTERN.matchEntire(queryParams["title"])?.groupOrNull(),
                            )
                        )
                    }
                }
                return@buildParseResult
            }
        }
    }

    override fun toString() = "UrbiHtmlInput"
}
