package page.ooooo.geoshare.data.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import page.ooooo.geoshare.data.local.database.AppDatabase
import page.ooooo.geoshare.data.local.database.InitialLinks
import page.ooooo.geoshare.data.local.database.InitialServersImpl
import page.ooooo.geoshare.data.local.database.LinkDao
import page.ooooo.geoshare.data.local.database.ServerDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    fun provideLinkDao(appDatabase: AppDatabase): LinkDao {
        return appDatabase.getLinkDao()
    }

    @Provides
    fun provideServerDao(appDatabase: AppDatabase): ServerDao {
        return appDatabase.getServerDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "Link")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    InitialLinks.restore(db)
                    InitialServersImpl.restore(db)
                }
            })
            .addMigrations(
                *InitialLinks.migrations,
                *InitialServersImpl.migrations,
            )
            .build()
    }
}
