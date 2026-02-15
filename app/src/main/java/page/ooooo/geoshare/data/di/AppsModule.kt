package page.ooooo.geoshare.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import page.ooooo.geoshare.data.AppsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppsModule {

    @Provides
    @Singleton
    fun provideAppsRepository(
        @ApplicationScope applicationScope: CoroutineScope,
        @ApplicationContext context: Context,
    ): AppsRepository =
        AppsRepository(applicationScope, context)
}
