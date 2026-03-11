package com.callshield.app.core.billing

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.callshield.app.core.util.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_DONE = booleanPreferencesKey("onboarding_done")
    }

    val isOnboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_DONE] ?: false }

    suspend fun markDone() {
        context.dataStore.edit { it[KEY_DONE] = true }
    }
}
