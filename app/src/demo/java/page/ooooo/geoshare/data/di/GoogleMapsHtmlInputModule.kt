package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInput
import page.ooooo.geoshare.lib.inputs.GoogleMapsHtmlInputImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleMapsHtmlInputModule {

    @Provides
    @Singleton
    fun provideGoogleMapsHtmlInput(): GoogleMapsHtmlInput = GoogleMapsHtmlInputImpl()
}
