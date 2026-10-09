package page.ooooo.geoshare.tests

import page.ooooo.geoshare.data.local.database.InitialServersImpl.GOOGLE_MAPS_GEOCODE_ADDRESS_UUID
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.database.ServerAuthType
import java.util.UUID

object InitialServersTestDataImpl : InitialServersTestData {
    override val expectedItems = buildList {
        add(
            Server(
                name = "Google Maps Geocode Address",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/address/{q}",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
                uuid = UUID.fromString(GOOGLE_MAPS_GEOCODE_ADDRESS_UUID),
            )
        )
        add(
            Server(
                name = "Google Maps Geocode Place",
                urlTemplate = "https://geocode.googleapis.com/v4/geocode/places/{q}",
                authType = ServerAuthType.API_KEY,
                apiKeyHeader = "X-Goog-Api-Key",
                uuid = UUID.fromString("c5c215a1-c453-4de9-adb3-daecbd7dc876"),
            )
        )
    }.sortedWith(compareBy<Server>{ it.name }.thenBy { it.description })
}
