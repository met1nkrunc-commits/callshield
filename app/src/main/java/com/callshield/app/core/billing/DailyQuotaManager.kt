package com.callshield.app.core.billing

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.callshield.app.core.util.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyQuotaManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_DATE  = longPreferencesKey("quota_date")
        private val KEY_COUNT = intPreferencesKey("quota_count")
        const val FREE_DAILY_LIMIT = 5
    }

    private fun todayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val remainingQuota: Flow<Int> = context.dataStore.data.map { prefs ->
        val savedDate = prefs[KEY_DATE] ?: 0L
        val count     = prefs[KEY_COUNT] ?: 0
        if (savedDate == todayStart()) (FREE_DAILY_LIMIT - count).coerceAtLeast(0)
        else FREE_DAILY_LIMIT
    }

    suspend fun hasQuota(): Boolean {
        var quota = FREE_DAILY_LIMIT
        context.dataStore.data.first().let { prefs ->
            val savedDate = prefs[KEY_DATE] ?: 0L
            val count = prefs[KEY_COUNT] ?: 0
            quota = if (savedDate == todayStart()) FREE_DAILY_LIMIT - count else FREE_DAILY_LIMIT
        }
        return quota > 0
    }

    suspend fun consume() {
        context.dataStore.edit { prefs ->
            val today = todayStart()
            val savedDate = prefs[KEY_DATE] ?: 0L
            if (savedDate != today) {
                prefs[KEY_DATE]  = today
                prefs[KEY_COUNT] = 1
            } else {
                prefs[KEY_COUNT] = (prefs[KEY_COUNT] ?: 0) + 1
            }
        }
    }
}
