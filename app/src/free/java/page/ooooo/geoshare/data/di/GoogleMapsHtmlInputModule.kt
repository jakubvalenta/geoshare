package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
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
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
    ): GoogleMapsHtmlInput<*> =
        GoogleMapsHtmlInputImpl(
            googleMapsUriInput = { googleMapsUriInput },
            googleMapsWebViewInput = { googleMapsWebViewInput },
            engine = engine,
            log = log,
            uriQuote = uriQuote,
        )
}
