package page.ooooo.geoshare.lib.android

object BaiduMapAppType : GeoUriAppType {
    const val PACKAGE_NAME = "com.baidu.BaiduMap"

    override fun matches(packageName: String) = packageName == PACKAGE_NAME

    override val params = GeoUriAppType.Params(nameSupported = false, pinSupported = false, zoomAndQSupported = false)

    /**
     * Notice that Baidu Map supports WGS84 geo: URIs, not BD09MC or GCJ02 as one might expect.
     */
    override val srs = GeoUriAppType.Srs.WGS84
}
