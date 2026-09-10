package page.ooooo.geoshare.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import page.ooooo.geoshare.data.AppsRepository
import page.ooooo.geoshare.lib.android.App
import page.ooooo.geoshare.lib.android.DataType
import page.ooooo.geoshare.lib.android.PackageNames
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppsModule {

    @Provides
    @Singleton
    fun provideAppsRepository(
        @ApplicationScope applicationScope: CoroutineScope,
        @ApplicationContext context: Context,
    ): AppsRepository =
        AppsRepository(applicationScope, context)
}

val fakeApps = mapOf(
    PackageNames.COMAPS_FDROID to App(
        packageName = PackageNames.COMAPS_FDROID,
        dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
    ),
    PackageNames.CONVERSATIONS to App(
        packageName = PackageNames.CONVERSATIONS,
        dataTypes = setOf(DataType.SEND_PLAIN_TEXT)
    ),
    PackageNames.GMAPS_WV to App(
        packageName = PackageNames.GMAPS_WV,
        dataTypes = setOf(DataType.GEO_URI)
    ),
    PackageNames.GOOGLE_MAPS to App(
        packageName = PackageNames.GOOGLE_MAPS,
        dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
    ),
    PackageNames.HERE_WEGO to App(
        packageName = PackageNames.HERE_WEGO,
        dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
    ),
    PackageNames.MAGIC_EARTH to App(
        packageName = PackageNames.MAGIC_EARTH,
        dataTypes = setOf(DataType.MAGIC_EARTH_URI)
    ),
    PackageNames.MAPY_COM to App(
        packageName = PackageNames.MAPY_COM,
        dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
    ),
    PackageNames.ORGANIC_MAPS to App(
        packageName = PackageNames.ORGANIC_MAPS,
        dataTypes = setOf(DataType.GEO_URI, DataType.GOOGLE_NAVIGATION_URI)
    ),
    PackageNames.OSMAND_PLUS to App(
        packageName = PackageNames.OSMAND_PLUS,
        dataTypes = setOf(DataType.GPX_DATA)
    ),
    PackageNames.TOMTOM to App(
        packageName = PackageNames.TOMTOM,
        dataTypes = setOf(DataType.GPX_ONE_POINT_DATA)
    ),
)
