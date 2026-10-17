package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.LayoutDirection
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.android.openUriInDefaultApp
import page.ooooo.geoshare.ui.theme.defaultLinkStyles
import java.util.Calendar

interface InputGroup {
    val id: String

    fun getName(resources: Resources): String

    interface HasAttribution {
        fun getName(resources: Resources): String

        @Composable
        fun Attribution() {
            val layoutDirection = LocalLayoutDirection.current
            val resources = LocalResources.current
            Text(
                resources.getString(R.string.copyright, getName(resources)),
                style = LocalTextStyle.current.copy(
                    // Don't detect layout direction from content, because it can fail due to the group name being in LTR
                    textDirection = if (layoutDirection == LayoutDirection.Rtl) TextDirection.Rtl else TextDirection.Ltr
                ),
            )
        }
    }

    companion object {
        val currentYear by lazy {
            // Use Calendar to support API < 26
            Calendar.getInstance().get(Calendar.YEAR)
        }
    }
}

object AmapInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "Amap"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_amap_name)
}

object AppleMapsInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "AppleMaps"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_apple_maps_name)
}

object BaiduMapInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "BaiduMap"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_baidu_map_name)
}

object CartesIGNInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "CartesIGN"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_cartes_ign_name)
}

object CoordinatesInputGroup : InputGroup {
    override val id = "Coordinates"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_coordinates_name)
}

object DebugInputGroup : InputGroup {
    override val id = "Debug"

    override fun getName(resources: Resources) = "Debug Input"
}

object GeoUriInputGroup : InputGroup {
    override val id = "GeoUri"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_geo_name)
}

object GoogleMapsInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "GoogleMaps"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_google_maps_name)

    @Composable
    override fun Attribution() {
        // See https://developers.google.com/maps/documentation/places/web-service/policies
        Text(
            "Google Maps", // Must not be translated
            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1F1F1F),
        )
    }
}

object GoogleNavigationUriInputGroup : InputGroup {
    override val id = "GoogleNavigationUri"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_google_navigation_uri_name)
}

object HereWeGoInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "HereWego"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_here_wego_name)

    @Composable
    override fun Attribution() {
        // See https://docs.here.com/map-rendering/docs/required-copyright-notice-vector-tile-api
        Text(
            "© ${InputGroup.currentYear} HERE" // Must not be translated
        )
    }
}

object KagiMapsInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "Kagi Maps"

    override fun getName(resources: Resources) =
        resources.getString(R.string.input_kagi_maps_name)
}

object MagicEarthInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "Magic Earth"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_magic_earth_name)
}

object MapsMeInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "MapsMe"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_ge0_name)

    @Composable
    override fun Attribution() = OpenStreetMapInputGroup.Attribution()
}

object MapyComInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "MapyCom"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_mapy_com_name)
}

object MapQuestInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "MapQuest"

    override fun getName(resources: Resources) =
        resources.getString(R.string.input_map_quest_name)
}

object OpenStreetMapInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "OpenStreetMap"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_open_street_map_name)

    @Composable
    override fun Attribution() {
        // See https://www.openstreetmap.org/copyright
        val context = LocalContext.current
        Text(buildAnnotatedString {
            append("Map data from ") // Probably shouldn't be translated
            withLink(
                LinkAnnotation.Clickable(tag = "link", styles = defaultLinkStyles()) {
                    context.openUriInDefaultApp("https://www.openstreetmap.org/copyright")
                }
            ) {
                append("OpenStreetMap") // Must not be translated
            }
        })
    }
}

object OsmAndInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "OsmAnd"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_osm_and_name)

    @Composable
    override fun Attribution() = OpenStreetMapInputGroup.Attribution()
}

object PlusCodeInputGroup : InputGroup {
    override val id = "PlusCode"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_plus_code_name)
}

object UrbiInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "Urbi"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_urbi_name)
}

object WazeInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "Waze"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_waze_name)
}

object YahooMapsInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "YahooMaps"

    override fun getName(resources: Resources) =
        resources.getString(R.string.input_yahoo_maps_name)
}

object YandexMapsInputGroup : InputGroup, InputGroup.HasAttribution {
    override val id = "YandexMaps"

    override fun getName(resources: Resources) =
        resources.getString(R.string.converter_yandex_maps_name)
}
