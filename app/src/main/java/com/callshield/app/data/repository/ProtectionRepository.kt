package com.callshield.app.data.repository

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
class ProtectionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val KEY = booleanPreferencesKey("protection_enabled")

    val isEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY] ?: true }

    suspend fun toggle() {
        context.dataStore.edit { prefs ->
            prefs[KEY] = !(prefs[KEY] ?: true)
        }
    }

    suspend fun set(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY] = enabled }
    }
}
