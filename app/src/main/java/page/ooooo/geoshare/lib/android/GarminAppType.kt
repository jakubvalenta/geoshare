package page.ooooo.geoshare.lib.android

object GarminAppType : GeoUriAppType {
    override fun matches(packageName: String) = packageName.startsWith("com.garmin.")
    override val params = GeoUriAppType.Params(zoomSupported = false)
    override val srs = GeoUriAppType.Srs.WGS84
}
