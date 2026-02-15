package page.ooooo.geoshare.lib.android

interface GeoUriAppType : AppType {

    data class Params(
        val nameSupported: Boolean = true,
        val pinSupported: Boolean = true,
        val zoomSupported: Boolean = true,
        /**
         * True if the app supports a geo: URI that has both 'z' and 'q' params set
         */
        val zoomAndQSupported: Boolean = true,
    ) {
        companion object {
            val Default = Params()
        }
    }

    enum class Srs { GCJ02, WGS84 }

    fun matches(packageName: String): Boolean

    val params: Params
    val srs: Srs
}
