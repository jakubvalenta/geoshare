package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.inputs.GoogleMapsAddressApiInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInputImpl
import page.ooooo.geoshare.lib.inputs.GoogleMapsPlaceApiInput
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsHtmlInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsHtmlInput(
        googleMapsAddressApiInput: GoogleMapsAddressApiInput,
        googleMapsPlaceApiInput: GoogleMapsPlaceApiInput,
    ): GoogleMapsHtmlInput<*> =
        GoogleMapsHtmlInputImpl(
            googleMapsAddressApiInput = { googleMapsAddressApiInput },
            googleMapsPlaceApiInput = { googleMapsPlaceApiInput },
        )
}
