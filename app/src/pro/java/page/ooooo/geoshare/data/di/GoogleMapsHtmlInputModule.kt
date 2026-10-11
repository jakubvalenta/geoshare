package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.data.uri.UriQuote
import page.ooooo.geoshare.data.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.data.inputs.GoogleMapsHtmlInputImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsHtmlInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsHtmlInput(uriQuote: UriQuote): GoogleMapsHtmlInput = GoogleMapsHtmlInputImpl(uriQuote)
}
