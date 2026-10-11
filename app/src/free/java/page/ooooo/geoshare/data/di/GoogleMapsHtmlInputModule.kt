package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.data.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.data.inputs.GoogleMapsHtmlInputImpl
import page.ooooo.geoshare.data.inputs.GoogleMapsUriInput
import page.ooooo.geoshare.data.inputs.GoogleMapsWebViewInput
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsHtmlInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsHtmlInput(
        googleMapsUriInput: GoogleMapsUriInput,
        googleMapsWebViewInput: GoogleMapsWebViewInput,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
    ): GoogleMapsHtmlInput =
        GoogleMapsHtmlInputImpl(
            googleMapsUriInput = { googleMapsUriInput },
            googleMapsWebViewInput = { googleMapsWebViewInput },
            engine = engine,
            log = log,
            uriQuote = uriQuote,
        )
}
