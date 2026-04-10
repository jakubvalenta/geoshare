package page.ooooo.geoshare.lib.geo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.prep.PreparedGeometry
import org.locationtech.jts.geom.prep.PreparedGeometryFactory
import org.locationtech.jts.io.WKBReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChinaGeometry @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val geometryFactory = GeometryFactory()

    private val preparedGeometry: PreparedGeometry by lazy {
        val bytes = context.assets.open("china.wkb").readBytes()
        val geometry = WKBReader(geometryFactory).read(bytes)
        PreparedGeometryFactory.prepare(geometry)
    }

    /**
     * Returns true if the given coordinates are within the China geometry.
     *
     * Loads and prepares the geometry from assets on the first call.
     *
     * @throws ExceptionInInitializerError if the asset file cannot be read or the WKB is malformed
     */
    fun containsPoint(x: Double, y: Double): Boolean {
        val point = geometryFactory.createPoint(Coordinate(x, y))
        return preparedGeometry.contains(point)
    }
}
