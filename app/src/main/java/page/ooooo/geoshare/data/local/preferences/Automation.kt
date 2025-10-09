package page.ooooo.geoshare.data.local.preferences

data class Automation(
    val type: Type,
    val packageName: String? = null,
) {
    enum class Type {
        COPY_APPLE_MAPS_URI,
        COPY_COORDS_DEC,
        COPY_COORDS_NSWE_DEC,
        COPY_GEO_URI,
        COPY_GOOGLE_MAPS_URI,
        COPY_MAGIC_EARTH_URI,
        NOTHING,
        OPEN_APP,
        SAVE_GPX,
        SHARE,
    }
}
