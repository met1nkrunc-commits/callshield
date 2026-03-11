package com.callshield.app.core.billing

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.callshield.app.core.util.BillingConstants
import com.callshield.app.core.util.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_PLAN = stringPreferencesKey("current_plan")
    }

    val currentPlan: Flow<String> = context.dataStore.data
        .map { it[KEY_PLAN] ?: BillingConstants.PLAN_FREE }

    suspend fun setPlan(plan: String) {
        context.dataStore.edit { it[KEY_PLAN] = plan }
    }

    suspend fun setFree() = setPlan(BillingConstants.PLAN_FREE)
}
