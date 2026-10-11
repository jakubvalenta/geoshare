package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.log.Log
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LogModule {

    @Singleton
    @Provides
    fun provideLog(): Log =
        DefaultLog
}
