package page.ooooo.geoshare.lib.android

object GoogleMapsAppType : GeoUriAppType {
    const val PACKAGE_NAME = "com.google.android.apps.maps"

    override fun matches(packageName: String) = packageName == PACKAGE_NAME
    override val params = GeoUriAppType.Params.Default
    override val srs = GeoUriAppType.Srs.GCJ02
}
