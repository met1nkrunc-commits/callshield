package com.callshield.app.core.di

import android.content.Context
import androidx.room.Room
import com.callshield.app.data.local.db.CallShieldDatabase
import com.callshield.app.data.local.db.dao.BlockEventDao
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.dao.BlockedSmsDao
import com.callshield.app.data.local.db.dao.NumberQueryCacheDao
import com.callshield.app.data.local.db.dao.PhishingUrlDao
import com.callshield.app.data.local.db.dao.TrustedNumberDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CallShieldDatabase =
        Room.databaseBuilder(context, CallShieldDatabase::class.java, "callshield.db")
            .addMigrations(
                // Fix #11 + #15: Migration zinciri tamamlandı.
                CallShieldDatabase.MIGRATION_1_2,
                CallShieldDatabase.MIGRATION_2_3,
                CallShieldDatabase.MIGRATION_3_4,
                CallShieldDatabase.MIGRATION_4_5,
                CallShieldDatabase.MIGRATION_5_6,
                CallShieldDatabase.MIGRATION_6_7,
            )
            .build()

    @Provides
    fun provideBlockedNumberDao(db: CallShieldDatabase): BlockedNumberDao =
        db.blockedNumberDao()

    @Provides
    fun provideBlockEventDao(db: CallShieldDatabase): BlockEventDao =
        db.blockEventDao()

    @Provides
    fun provideNumberQueryCacheDao(db: CallShieldDatabase): NumberQueryCacheDao =
        db.numberQueryCacheDao()

    @Provides
    fun provideTrustedNumberDao(db: CallShieldDatabase): TrustedNumberDao =
        db.trustedNumberDao()

    @Provides
    fun providePhishingUrlDao(db: CallShieldDatabase): PhishingUrlDao =
        db.phishingUrlDao()

    @Provides
    fun provideBlockedSmsDao(db: CallShieldDatabase): BlockedSmsDao =
        db.blockedSmsDao()
}
