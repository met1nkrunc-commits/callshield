package com.callshield.app.core.di

import com.callshield.app.data.repository.BlockEventRepositoryImpl
import com.callshield.app.data.repository.BlockedNumberRepositoryImpl
import com.callshield.app.data.repository.BlockedSmsRepositoryImpl
import com.callshield.app.data.repository.TrustedNumberRepositoryImpl
import com.callshield.app.domain.repository.BlockEventRepository
import com.callshield.app.domain.repository.BlockedNumberRepository
import com.callshield.app.domain.repository.BlockedSmsRepository
import com.callshield.app.domain.repository.TrustedNumberRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBlockedNumberRepository(
        impl: BlockedNumberRepositoryImpl,
    ): BlockedNumberRepository

    @Binds
    @Singleton
    abstract fun bindBlockEventRepository(
        impl: BlockEventRepositoryImpl,
    ): BlockEventRepository

    @Binds
    @Singleton
    abstract fun bindTrustedNumberRepository(
        impl: TrustedNumberRepositoryImpl,
    ): TrustedNumberRepository

    @Binds
    @Singleton
    abstract fun bindBlockedSmsRepository(
        impl: BlockedSmsRepositoryImpl,
    ): BlockedSmsRepository
}
