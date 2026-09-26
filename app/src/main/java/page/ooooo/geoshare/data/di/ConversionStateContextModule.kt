package page.ooooo.geoshare.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.geo.CoordinateConverter

/**
 * Injects [ConversionStateContext] into a view model.
 *
 * Notice that it uses the [ViewModelScoped] scope, so that it gets destroyed when finishing the activity. If we used
 * the [SingletonComponent] scope, then re-opening the app would load an old conversion state context while the other
 * state would be reset.
 */
@Module
@InstallIn(ViewModelComponent::class)
object ConversionStateContextModule {

    @Provides
    @ViewModelScoped
    fun provideConversionStateContext(
        @ApplicationContext context: Context,
        billing: Billing,
        coordinateConverter: CoordinateConverter,
        inputRepository: InputRepository,
        linkRepository: LinkRepository,
        log: Log,
        uriQuote: UriQuote,
        userPreferencesRepository: UserPreferencesRepository,
    ): ConversionStateContext =
        ConversionStateContext(
            coordinateConverter = coordinateConverter,
            inputs = inputRepository.all,
            linkRepository = linkRepository,
            resources = context.resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
}
