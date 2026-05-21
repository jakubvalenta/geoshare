package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.inputs.GoogleMapsPlaceListInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsPlaceListInputImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsPlaceListInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsPlaceListInput(log: Log): GoogleMapsPlaceListInput =
        GoogleMapsPlaceListInputImpl(log = log)
}
