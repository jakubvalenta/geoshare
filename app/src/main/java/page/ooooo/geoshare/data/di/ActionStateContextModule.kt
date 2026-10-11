package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.billing.Billing
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.state.ActionStateContext

@Module
@InstallIn(ViewModelComponent::class)
object ActionStateContextModule {

    @Provides
    @ViewModelScoped
    fun provideActionStateContext(
        billing: Billing,
        coordinateConverter: CoordinateConverter,
        linkRepository: LinkRepository,
        log: Log,
        userPreferencesRepository: UserPreferencesRepository,
    ): ActionStateContext =
        ActionStateContext(
            billing = billing,
            coordinateConverter = coordinateConverter,
            linkRepository = linkRepository,
            log = log,
            userPreferencesRepository = userPreferencesRepository,
        )
}
