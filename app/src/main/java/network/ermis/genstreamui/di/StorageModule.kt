package network.ermis.genstreamui.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import network.ermis.genstreamui.database.storage.AppDatabase
import network.ermis.genstreamui.database.storage.dao.RecentGameDao
import javax.inject.Singleton

/**
 * Cung cấp Room database + DAO. Port pattern từ GenPlayAndroid (di/DatabaseModule.kt).
 */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "GenStreamUI.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideRecentGameDao(database: AppDatabase): RecentGameDao = database.recentGameDao()
}
