package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.data.inputs.GoogleMapsPlaceListInput
import page.ooooo.geoshare.data.inputs.GoogleMapsPlaceListInputImpl
import page.ooooo.geoshare.data.log.Log
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsPlaceListInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsPlaceListInput(log: Log): GoogleMapsPlaceListInput =
        GoogleMapsPlaceListInputImpl(log = log)
}
