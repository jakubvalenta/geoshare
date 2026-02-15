package page.ooooo.geoshare.lib.android

@Suppress("SpellCheckingInspection")
object OeffiAppType : GeoUriAppType {
    const val PACKAGE_NAME = "de.schildbach.oeffi"
    override fun matches(packageName: String) = packageName == PACKAGE_NAME
    override val params = GeoUriAppType.Params(nameSupported = false)
    override val srs = GeoUriAppType.Srs.WGS84
}
