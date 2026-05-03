package com.callshield.app.core.di

import com.bengel.shared.ml.TurkishPatternMatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideTurkishPatternMatcher(): TurkishPatternMatcher = TurkishPatternMatcher()
}
