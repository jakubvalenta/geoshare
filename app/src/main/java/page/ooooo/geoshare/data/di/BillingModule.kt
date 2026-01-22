package page.ooooo.geoshare.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.billing.BillingImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBilling(@ApplicationContext context: Context): Billing {
        return BillingImpl(context)
    }
}
