package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

@Module
@InstallIn(SingletonComponent::class)
class EngineModule {
    @Provides
    fun provideEngine(): HttpClientEngine =
        CIO.create()
}
