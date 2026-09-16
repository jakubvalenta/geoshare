package page.ooooo.geoshare.data.local.preferences

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.UUIDSerializer
import java.util.UUID

@Serializable
sealed interface Automation

sealed interface BasicAutomation : Automation

sealed interface LinkAutomation : Automation {
    val linkUUID: UUID
}

@Serializable
@SerialName("COPY_COORDS_DEC")
object CopyCoordsDecAutomation : BasicAutomation

@Serializable
@SerialName("COPY_COORDS_NSWE_DEC")
object CopyCoordsDegMinSecAutomation : BasicAutomation

@Serializable
@SerialName("COPY_GEO_URI")
object CopyGeoUriAutomation : BasicAutomation

@Serializable
@SerialName("COPY_LINK_URI")
data class CopyLinkUriAutomation(@Serializable(with = UUIDSerializer::class) override val linkUUID: UUID) :
    LinkAutomation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_APPLE_MAPS_URI")
object CopyLinkDisplayAppleMapsUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString(InitialLinks.APPLE_MAPS_DISPLAY_UUID)
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_URI")
object CopyLinkDisplayGoogleMapsUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString(InitialLinks.GOOGLE_MAPS_DISPLAY_UUID)
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_MAGIC_EARTH_URI")
object CopyLinkDisplayMagicEarthUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07")
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_APPLE_MAPS_NAVIGATE_TO_URI")
object CopyLinkNavigationAppleMapsUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString(InitialLinks.APPLE_MAPS_NAVIGATION_UUID)
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_NAVIGATE_TO_URI")
object CopyLinkNavigationGoogleUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a")
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_MAGIC_EARTH_NAVIGATE_TO_URI")
object CopyLinkNavigationMagicEarthUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7")
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_STREET_VIEW_URI")
object CopyLinkStreetViewGoogleUriAutomation : LinkAutomation {
    override val linkUUID: UUID = UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a")
}

@Serializable
@SerialName("COPY_NAME")
object CopyNameAutomation : BasicAutomation

@Serializable
@SerialName("NOOP")
object NoopAutomation : BasicAutomation

@Serializable
@SerialName("OPEN_APP")
data class OpenDisplayGeoUriAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_DISPLAY_CARTES_IGN_URL")
data class OpenDisplayCartesIGNUrlAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_DISPLAY_MAGIC_EARTH_URI")
data class OpenDisplayMagicEarthUriAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO")
data class OpenNavigationGoogleUriAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_APP_MAGIC_EARTH_NAVIGATE_TO")
data class OpenNavigationMagicEarthUriAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_APP_GOOGLE_MAPS_STREET_VIEW")
data class OpenStreetViewGoogleUriAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_GPX_POINTS")
data class OpenPointsGpxAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_GPX_ROUTE_MANY")
data class OpenRouteGpxAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("OPEN_APP_GPX_ROUTE")
data class OpenRouteOnePointGpxAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("SAVE_GPX_POINT")
object SavePointGpxAutomation : BasicAutomation

@Serializable
@SerialName("SAVE_GPX")
object SavePointsGpxAutomation : BasicAutomation

@Serializable
@SerialName("SAVE_GPX_ROUTE_MANY")
object SaveRouteGpxAutomation : BasicAutomation

@Serializable
@SerialName("SAVE_POINT_TO_CONTACT")
object SavePointToContactAutomation : BasicAutomation

@Serializable
@SerialName("SEND_POINT")
data class SendPointAutomation(val packageName: String?) : BasicAutomation

@Serializable
@SerialName("SHARE")
object ShareDisplayGeoUriAutomation : BasicAutomation

@Serializable
@SerialName("SHARE_LINK_URI")
data class ShareLinkUriAutomation(@Serializable(with = UUIDSerializer::class) override val linkUUID: UUID) :
    LinkAutomation

@Serializable
@SerialName("SHARE_GPX_ROUTE")
object ShareRouteGpxAutomation : BasicAutomation

@Serializable
@SerialName("SHARE_POINTS_GPX")
object SharePointsGpxAutomation : BasicAutomation

@Serializable
@SerialName("SHARE_NAVIGATION_GOOGLE_URI")
object ShareNavigationGoogleUriAutomation : BasicAutomation

@Serializable
@SerialName("SHARE_STREET_VIEW_GOOGLE_URI")
object ShareStreetViewGoogleUriAutomation : BasicAutomation
