package com.callshield.app.core.di

import android.content.Context
import com.callshield.app.core.billing.BillingManager
import com.callshield.app.core.billing.DailyQuotaManager
import com.callshield.app.core.billing.OnboardingManager
import com.callshield.app.core.billing.PlanManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Fix #16: AppModule — ARCHITECTURE.md'de tanımlıydı ama oluşturulmamıştı.
 * Uygulama genelinde singleton olan yardımcı bileşenler burada sağlanır.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePlanManager(@ApplicationContext context: Context): PlanManager =
        PlanManager(context)

    @Provides
    @Singleton
    fun provideDailyQuotaManager(@ApplicationContext context: Context): DailyQuotaManager =
        DailyQuotaManager(context)

    @Provides
    @Singleton
    fun provideOnboardingManager(@ApplicationContext context: Context): OnboardingManager =
        OnboardingManager(context)

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context,
        planManager: PlanManager,
    ): BillingManager = BillingManager(context, planManager)
}
