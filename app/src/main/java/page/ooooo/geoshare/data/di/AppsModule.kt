package page.ooooo.geoshare.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import page.ooooo.geoshare.data.AppsRepository
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.FileType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.TextActivity
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
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

val fakeActivities = listOf(
    UriActivity(PackageNames.COMAPS_FDROID, UriScheme.GEO),
    UriActivity(PackageNames.COMAPS_FDROID, UriScheme.GOOGLE_NAVIGATION),
    TextActivity(PackageNames.CONVERSATIONS, mimeType = "text/plain"),
    UriActivity(PackageNames.GMAPS_WV, UriScheme.GEO),
    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GEO),
    UriActivity(PackageNames.GOOGLE_MAPS, UriScheme.GOOGLE_NAVIGATION),
    UriActivity(PackageNames.HERE_WEGO, UriScheme.GEO),
    UriActivity(PackageNames.HERE_WEGO, UriScheme.GOOGLE_NAVIGATION),
    UriActivity(PackageNames.MAGIC_EARTH, UriScheme.MAGIC_EARTH),
    UriActivity(PackageNames.MAPY_COM, UriScheme.GEO),
    UriActivity(PackageNames.MAPY_COM, UriScheme.GOOGLE_NAVIGATION),
    UriActivity(PackageNames.ORGANIC_MAPS, UriScheme.GEO),
    UriActivity(PackageNames.ORGANIC_MAPS, UriScheme.GOOGLE_NAVIGATION),
    FileActivity(PackageNames.OSMAND_PLUS, FileType.GPX),
    FileActivity(PackageNames.TOMTOM, FileType.GPX_ONE_POINT),
)
