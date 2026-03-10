package page.ooooo.geoshare.data.local.preferences

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.UUIDSerializer
import page.ooooo.geoshare.lib.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.lib.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.lib.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.lib.outputs.NoopOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.OpenNavigationMagicEarthUriOutput
import page.ooooo.geoshare.lib.outputs.OpenPointsGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.lib.outputs.OpenStreetViewGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.Output
import page.ooooo.geoshare.lib.outputs.SavePointGpxOutput
import page.ooooo.geoshare.lib.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.lib.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.lib.outputs.ShareStreetViewGoogleUriOutput
import java.util.UUID

@Serializable
sealed interface Automation {
    suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?): Output?
}

@Serializable
@SerialName("COPY_COORDS_DEC")
object CopyCoordsDecAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = CopyCoordsDecOutput
}

@Serializable
@Suppress("SpellCheckingInspection")
@SerialName("COPY_COORDS_NSWE_DEC")
object CopyCoordsDegMinSecAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = CopyCoordsDegMinSecOutput
}

@Serializable
@SerialName("COPY_GEO_URI")
object CopyGeoUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = CopyGeoUriOutput
}

@Serializable
@SerialName("COPY_LINK_URI")
data class CopyLinkUriAutomation(
    @Serializable(with = UUIDSerializer::class) val linkUUID: UUID,
) :
    Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(linkUUID)?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_APPLE_MAPS_URI")
object CopyLinkDisplayAppleMapsUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("ce900ea1-2c5d-4641-82f3-a5429a68d603"))?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_URI")
object CopyLinkDisplayGoogleMapsUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("7bd96da4-beba-4a30-9dbd-b437a49a1dc0"))?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_MAGIC_EARTH_URI")
object CopyLinkDisplayMagicEarthUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("b109970a-aef8-4482-9879-52e128fd0e07"))?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_APPLE_MAPS_NAVIGATE_TO_URI")
object CopyLinkNavigationAppleMapsUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("a5092c63-cf5c-4225-9059-e888ae12e215"))?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_NAVIGATE_TO_URI")
object CopyLinkNavigationGoogleUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("64b0b360-24ec-4113-9056-314223c6e19a"))?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_MAGIC_EARTH_NAVIGATE_TO_URI")
object CopyLinkNavigationMagicEarthUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("ee4f961c-44b0-4cb6-baad-1ed28edb8ec7"))?.let { link -> CopyLinkUriOutput(link) }
}

@Deprecated("Replaced with CopyLinkUriAutomation")
@Serializable
@SerialName("COPY_GOOGLE_MAPS_STREET_VIEW_URI")
object CopyLinkStreetViewGoogleUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(UUID.fromString("9d7cd113-ce01-4b8b-82fe-856956b8b20a"))?.let { link -> CopyLinkUriOutput(link) }
}

@Serializable
@SerialName("NOOP")
object NoopAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = NoopOutput
}

@Serializable
@SerialName("OPEN_APP")
data class OpenDisplayGeoUriAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenDisplayGeoUriOutput(packageName) }
}

@Serializable
@SerialName("OPEN_DISPLAY_MAGIC_EARTH_URI")
data class OpenDisplayMagicEarthUriAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenDisplayMagicEarthUriOutput(packageName) }
}

@Serializable
@SerialName("OPEN_APP_GOOGLE_MAPS_NAVIGATE_TO")
data class OpenNavigationGoogleUriAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenNavigationGoogleUriOutput(packageName) }
}

@Serializable
@SerialName("OPEN_APP_MAGIC_EARTH_NAVIGATE_TO")
data class OpenNavigationMagicEarthUriAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenNavigationMagicEarthUriOutput(packageName) }
}

@Serializable
@SerialName("OPEN_APP_GOOGLE_MAPS_STREET_VIEW")
data class OpenStreetViewGoogleUriAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenStreetViewGoogleUriOutput(packageName) }
}

@Serializable
@SerialName("OPEN_GPX_POINTS")
data class OpenPointsGpxAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenPointsGpxOutput(packageName) }
}

@Serializable
@SerialName("OPEN_GPX_ROUTE_MANY")
data class OpenRouteGpxAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenRouteGpxOutput(packageName) }
}

@Serializable
@SerialName("OPEN_APP_GPX_ROUTE")
data class OpenRouteOnePointGpxAutomation(
    val packageName: String?,
) : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        packageName?.let { packageName -> OpenRouteOnePointGpxOutput(packageName) }
}

@Serializable
@SerialName("SAVE_GPX_POINT")
object SavePointGpxAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = SavePointGpxOutput
}

@Serializable
@SerialName("SAVE_GPX")
object SavePointsGpxAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = SavePointsGpxOutput
}

@Serializable
@SerialName("SAVE_GPX_ROUTE_MANY")
object SaveRouteGpxAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = SaveRouteGpxOutput
}

@Serializable
@SerialName("SHARE")
object ShareDisplayUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = ShareDisplayGeoUriOutput
}

@Serializable
@SerialName("SHARE_LINK_URI")
data class ShareLinkUriAutomation(
    @Serializable(with = UUIDSerializer::class) val linkUUID: UUID,
) :
    Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) =
        getLinkByUUID(linkUUID)?.let { link -> ShareLinkUriOutput(link) }
}

@Serializable
@SerialName("SHARE_GPX_ROUTE")
object ShareRouteGpxAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = ShareRouteGpxOutput
}

@Serializable
@SerialName("SHARE_POINTS_GPX")
object SharePointsGpxAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = SharePointsGpxOutput
}

@Serializable
@SerialName("SHARE_NAVIGATION_GOOGLE_URI")
object ShareNavigationGoogleUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = ShareNavigationGoogleUriOutput
}

@Serializable
@SerialName("SHARE_STREET_VIEW_GOOGLE_URI")
object ShareStreetViewGoogleUriAutomation : Automation {
    override suspend fun toOutput(getLinkByUUID: suspend (linkUUID: UUID) -> Link?) = ShareStreetViewGoogleUriOutput
}
