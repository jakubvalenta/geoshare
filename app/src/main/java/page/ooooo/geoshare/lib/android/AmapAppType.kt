package page.ooooo.geoshare.lib.android

object AmapAppType : GeoUriAppType {
    @Suppress("SpellCheckingInspection")
    const val PACKAGE_NAME = "com.autonavi.minimap"
    override fun matches(packageName: String) = packageName == PACKAGE_NAME
    override val params = GeoUriAppType.Params.Default
    override val srs = GeoUriAppType.Srs.GCJ02
}
