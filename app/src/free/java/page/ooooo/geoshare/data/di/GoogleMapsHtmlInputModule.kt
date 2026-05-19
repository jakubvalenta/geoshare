package page.ooooo.geoshare.lib.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInputImpl
import page.ooooo.geoshare.lib.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsWebViewInput
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsHtmlInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsHtmlInput(
        googleMapsUriInput: GoogleMapsUriInput,
        googleMapsWebViewInput: GoogleMapsWebViewInput,
    ): GoogleMapsHtmlInput<*> =
        GoogleMapsHtmlInputImpl(
            googleMapsUriInput = { googleMapsUriInput },
            googleMapsWebViewInput = { googleMapsWebViewInput },
        )
}
