package page.ooooo.geoshare.lib.android

val geoUriAppTypes = listOf(
    AmapAppType,
    BaiduMapAppType,
    GMapsWVAppType,
    GarminAppType,
    GoogleMapsAppType,
    OeffiAppType,
)

fun List<GeoUriAppType>.getByPackageName(packageName: String): GeoUriAppType =
    this.firstOrNull { it.matches(packageName) } ?: DefaultGeoUriAppType
