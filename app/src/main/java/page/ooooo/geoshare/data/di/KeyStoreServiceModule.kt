package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.android.KeyStoreService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KeyStoreServiceModule {

    @Singleton
    @Provides
    fun provideKeyStoreService(): KeyStoreService =
        KeyStoreService()
}
