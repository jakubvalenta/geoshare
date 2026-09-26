package page.ooooo.geoshare.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.outputs.ActionStateContext

@Module
@InstallIn(ViewModelComponent::class)
object ActionStateContextModule {

    @Provides
    @ViewModelScoped
    fun provideActionStateContext(
        userPreferencesRepository: UserPreferencesRepository,
    ): ActionStateContext =
        ActionStateContext(
            userPreferencesRepository = userPreferencesRepository,
        )
}
