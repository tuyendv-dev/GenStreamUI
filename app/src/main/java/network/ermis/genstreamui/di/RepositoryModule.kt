package network.ermis.genstreamui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import network.ermis.genstreamui.database.network.repository.AuthRepositoryImpl
import network.ermis.genstreamui.database.network.repository.UserRepositoryImpl
import network.ermis.genstreamui.domain.repository.AuthRepository
import network.ermis.genstreamui.domain.repository.UserRepository
import javax.inject.Singleton

/**
 * Bind các interface repository (domain) tới implementation (data) bằng @Binds (zero-cost).
 * Port pattern từ GenPlayAndroid (di/RepositoryModule.kt).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
