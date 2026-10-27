package page.ooooo.geoshare.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferenceRepository
import page.ooooo.geoshare.data.billing.Billing
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.state.ActionStateContext

@Module
@InstallIn(ViewModelComponent::class)
object ActionStateContextModule {

    @Provides
    @ViewModelScoped
    fun provideActionStateContext(
        @ApplicationContext context: Context,
        billing: Billing,
        coordinateConverter: CoordinateConverter,
        linkRepository: LinkRepository,
        log: Log,
        userPreferenceRepository: UserPreferenceRepository,
    ): ActionStateContext =
        ActionStateContext(
            billing = billing,
            context = context,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferenceRepository = userPreferenceRepository,
        )
}
