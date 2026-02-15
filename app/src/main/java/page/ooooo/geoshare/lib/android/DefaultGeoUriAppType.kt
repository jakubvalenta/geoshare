package page.ooooo.geoshare.lib.android

object DefaultGeoUriAppType : GeoUriAppType {
    override fun matches(packageName: String) = true
    override val srs = GeoUriAppType.Srs.WGS84
    override val params = GeoUriAppType.Params()
}
