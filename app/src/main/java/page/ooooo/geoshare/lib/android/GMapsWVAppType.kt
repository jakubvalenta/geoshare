package page.ooooo.geoshare.lib.android

object GMapsWVAppType : GeoUriAppType {
    @Suppress("SpellCheckingInspection")
    const val PACKAGE_NAME = "us.spotco.maps"
    override fun matches(packageName: String) = packageName == PACKAGE_NAME
    override val params = GeoUriAppType.Params.Default
    override val srs = GeoUriAppType.Srs.GCJ02
}
