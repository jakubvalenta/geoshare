package page.ooooo.geoshare.data.database

import java.util.UUID

fun List<Link>.findByUUID(uuid: UUID): Link? = this.firstOrNull { it.uuid == uuid }
