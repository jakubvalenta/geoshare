package page.ooooo.geoshare.data.local.preferences

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.data.local.database.UUIDSerializer
import java.util.UUID

@Serializable
sealed interface Automation

@Serializable
@SerialName("COPY_COORDS_DEC")
object CopyCoordsDecAutomation : Automation

@Serializable
@Suppress("SpellCheckingInspection")
@SerialName("COPY_COORDS_NSWE_DEC")
object CopyCoordsDegMinSecAutomation : Automation

@Serializable
@SerialName("COPY_GEO_URI")
object CopyGeoUriAutomation : Automation

@Serializable
@SerialName("COPY_LINK_URI")
data class CopyLinkUriAutomation(@Serializable(with = UUIDSerializer::class) val linkUUID: UUID) : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_APPLE_MAPS_URI")
object CopyLinkDisplayAppleMapsUriAutomation : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_URI")
object CopyLinkDisplayGoogleMapsUriAutomation : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_MAGIC_EARTH_URI")
object CopyLinkDisplayMagicEarthUriAutomation : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_APPLE_MAPS_NAVIGATE_TO_URI")
object CopyLinkNavigationAppleMapsUriAutomation : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_NAVIGATE_TO_URI")
object CopyLinkNavigationGoogleUriAutomation : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_MAGIC_EARTH_NAVIGATE_TO_URI")
object CopyLinkNavigationMagicEarthUriAutomation : Automation

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_STREET_VIEW_URI")
object CopyLinkStreetViewGoogleUriAutomation : Automation

@Serializable
@SerialName("COPY_PLUS_CODE_URI")
object CopyPlusCodeUriAutomation : Automation

@Serializable
@SerialName("NOOP")
object NoopAutomation : Automation

@Serializable
@SerialName("OPEN_APP")
data class OpenDisplayGeoUriAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_DISPLAY_MAGIC_EARTH_URI")
data class OpenDisplayMagicEarthUriAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO")
data class OpenNavigationGoogleUriAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_APP_MAGIC_EARTH_NAVIGATE_TO")
data class OpenNavigationMagicEarthUriAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_APP_GOOGLE_MAPS_STREET_VIEW")
data class OpenStreetViewGoogleUriAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_GPX_POINTS")
data class OpenPointsGpxAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_GPX_ROUTE_MANY")
data class OpenRouteGpxAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("OPEN_APP_GPX_ROUTE")
data class OpenRouteOnePointGpxAutomation(val packageName: String?) : Automation

@Serializable
@SerialName("SAVE_GPX_POINT")
object SavePointGpxAutomation : Automation

@Serializable
@SerialName("SAVE_GPX")
object SavePointsGpxAutomation : Automation

@Serializable
@SerialName("SAVE_GPX_ROUTE_MANY")
object SaveRouteGpxAutomation : Automation

@Serializable
@SerialName("SHARE")
object ShareDisplayGeoUriAutomation : Automation

@Serializable
@SerialName("SHARE_LINK_URI")
data class ShareLinkUriAutomation(@Serializable(with = UUIDSerializer::class) val linkUUID: UUID) : Automation

@Serializable
@SerialName("SHARE_GPX_ROUTE")
object ShareRouteGpxAutomation : Automation

@Serializable
@SerialName("SHARE_POINTS_GPX")
object SharePointsGpxAutomation : Automation

@Serializable
@SerialName("SHARE_NAVIGATION_GOOGLE_URI")
object ShareNavigationGoogleUriAutomation : Automation

@Serializable
@SerialName("SHARE_STREET_VIEW_GOOGLE_URI")
object ShareStreetViewGoogleUriAutomation : Automation
