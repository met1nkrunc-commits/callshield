package com.callshield.app.core.di

import android.content.Context
import androidx.room.Room
import com.callshield.app.data.local.db.CallShieldDatabase
import com.callshield.app.data.local.db.dao.BlockEventDao
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.dao.NumberQueryCacheDao
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
            .addMigrations(CallShieldDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
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
}
