package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.uri.DefaultUriQuote
import page.ooooo.geoshare.lib.uri.UriQuote
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UriQuoteModule {

    @Singleton
    @Provides
    fun provideUriQuote(): UriQuote =
        DefaultUriQuote
}
