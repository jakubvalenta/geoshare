package page.ooooo.geoshare.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import page.ooooo.geoshare.data.android.AppActivity
import page.ooooo.geoshare.data.android.AppDetails
import page.ooooo.geoshare.data.android.getPackageNames
import page.ooooo.geoshare.data.android.queryActivities
import page.ooooo.geoshare.data.android.queryActivitiesForUri
import page.ooooo.geoshare.data.android.queryAppDetails
import page.ooooo.geoshare.data.di.ApplicationScope
import javax.inject.Inject

interface AppRepository {
    val activities: StateFlow<List<AppActivity>?>
    val appDetails: StateFlow<AppDetails?>
    fun queryActivitiesForUri(uriString: String): List<AppActivity>
}

class DefaultAppRepository @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context,
) : AppRepository {
    override val activities =
        flow { emit(context.packageManager.queryActivities()) }
            .catch { emit(emptyList()) }
            .stateIn(applicationScope, SharingStarted.Lazily, null)
    override val appDetails =
        activities
            .filterNotNull()
            .map { activities -> context.packageManager.queryAppDetails(activities.getPackageNames()) }
            .stateIn(applicationScope, SharingStarted.Lazily, null)

    override fun queryActivitiesForUri(uriString: String): List<AppActivity> =
        context.packageManager.queryActivitiesForUri(uriString)
}
