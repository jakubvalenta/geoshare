package page.ooooo.geoshare.data.di

import android.annotation.SuppressLint
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.AppRepository
import page.ooooo.geoshare.data.DefaultAppRepository
import page.ooooo.geoshare.lib.android.AppDetail
import page.ooooo.geoshare.lib.android.FileActivity
import page.ooooo.geoshare.lib.android.FileType
import page.ooooo.geoshare.lib.android.PackageNames
import page.ooooo.geoshare.lib.android.TextActivity
import page.ooooo.geoshare.lib.android.UriActivity
import page.ooooo.geoshare.lib.android.UriScheme
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppRepositoryModule {

    @Singleton
    @Provides
    fun provideAppRepository(
        @ApplicationScope applicationScope: CoroutineScope,
        @ApplicationContext context: Context,
    ): AppRepository =
        DefaultAppRepository(applicationScope, context)
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

@SuppressLint("LocalContextGetResourceValueCall")
fun getFakeAppDetails(context: Context) =
    mapOf(
        PackageNames.COMAPS_FDROID to AppDetail(
            packageName = PackageNames.COMAPS_FDROID,
            label = "CoMaps",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.CONVERSATIONS to AppDetail(
            packageName = PackageNames.CONVERSATIONS,
            label = "Conversations",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.ORGANIC_MAPS to AppDetail(
            packageName = PackageNames.ORGANIC_MAPS,
            label = "Organic Maps",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.HERE_WEGO to AppDetail(
            packageName = PackageNames.HERE_WEGO,
            label = "HERE WeGo",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.MAPY_COM to AppDetail(
            packageName = PackageNames.MAPY_COM,
            label = "Mapy.com",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.OSMAND_PLUS to AppDetail(
            packageName = PackageNames.OSMAND_PLUS,
            label = "OsmAnd",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.MAGIC_EARTH to AppDetail(
            packageName = PackageNames.MAGIC_EARTH,
            label = "Magic Earth",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.GOOGLE_MAPS to AppDetail(
            packageName = PackageNames.GOOGLE_MAPS,
            label = "Google Maps",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.GMAPS_WV to AppDetail(
            packageName = PackageNames.GMAPS_WV,
            label = @Suppress("SpellCheckingInspection", "GrazieInspectionRunner") "GMaps WV",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
        PackageNames.TOMTOM to AppDetail(
            packageName = PackageNames.TOMTOM,
            label = "TomTom",
            icon = context.getDrawable(R.mipmap.ic_launcher_round)!!,
        ),
    )

class FakeAppRepository(val context: Context) : AppRepository {
    override val activities = flow { emit(fakeActivities) }
    override val appDetails = flow { emit(getFakeAppDetails(context)) }
}
