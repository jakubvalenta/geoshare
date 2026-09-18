package page.ooooo.geoshare.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import page.ooooo.geoshare.data.di.ApplicationScope
import page.ooooo.geoshare.lib.android.AppActivity
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.android.getPackageNames
import page.ooooo.geoshare.lib.android.queryActivities
import page.ooooo.geoshare.lib.android.queryAppDetails
import javax.inject.Inject

interface AppRepository {
    val activities: Flow<List<AppActivity>>
    val appDetails: Flow<AppDetails>
}

class DefaultAppRepository @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:ApplicationContext private val context: Context,
) : AppRepository {
    override val activities =
        flow { emit(context.packageManager.queryActivities()) }
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    override val appDetails =
        activities.map { activities -> context.packageManager.queryAppDetails(activities.getPackageNames()) }
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5000), replay = 1)
}
