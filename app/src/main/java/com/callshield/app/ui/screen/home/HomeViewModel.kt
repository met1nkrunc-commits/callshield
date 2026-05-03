package com.callshield.app.ui.screen.home

import android.app.role.RoleManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.CallLog
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.core.util.dataStore
import com.callshield.app.data.repository.ProtectionRepository
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.repository.BlockEventRepository
import com.callshield.app.domain.usecase.call.CheckNumberUseCase
import com.callshield.app.service.CallWarningNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val KEY_LAST_CALLLOG_CHECK   = longPreferencesKey("last_calllog_notif_check")
private val KEY_SMS_BANNER_DISMISSED = booleanPreferencesKey("sms_upgrade_banner_dismissed")

data class HomeUiState(
    val isProtectionEnabled: Boolean = true,
    val totalBlockedCount: Int = 0,
    val weeklyBlockedCount: Int = 0,
    val recentEvents: List<BlockEvent> = emptyList(),
    val isOffline: Boolean = false,
    val showSmsBanner: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockEventRepository: BlockEventRepository,
    private val protectionRepository: ProtectionRepository,
    private val checkNumberUseCase: CheckNumberUseCase,
) : ViewModel() {

    private val _smsBannerDismissed = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        blockEventRepository.getAll(),
        blockEventRepository.getWeeklyEvents(),
        protectionRepository.isEnabled,
        _smsBannerDismissed,
    ) { all, weekly, isActive, dismissed ->
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val offline = cm?.getNetworkCapabilities(cm.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true
        val isDefaultSms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
                .isRoleHeld(RoleManager.ROLE_SMS)
        } else false
        HomeUiState(
            isOffline           = offline,
            isProtectionEnabled = isActive,
            totalBlockedCount   = all.size,
            weeklyBlockedCount  = weekly.size,
            recentEvents        = all.sortedByDescending { it.timestamp }.take(5),
            showSmsBanner       = !isDefaultSms && !dismissed,
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val dismissed = context.dataStore.data
                .map { it[KEY_SMS_BANNER_DISMISSED] ?: false }
                .first()
            _smsBannerDismissed.value = dismissed
        }
        checkRecentCallsForRisk()
    }

    fun toggleProtection() {
        viewModelScope.launch { protectionRepository.toggle() }
    }

    fun dismissSmsBanner() {
        _smsBannerDismissed.value = true
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply { set(KEY_SMS_BANNER_DISMISSED, true) }
            }
        }
    }

    /** On every app open, scan call log of the last 24 h for new risky numbers and notify. */
    private fun checkRecentCallsForRisk() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lastCheck = context.dataStore.data
                    .map { it[KEY_LAST_CALLLOG_CHECK] ?: 0L }
                    .first()

                val fourHoursMs = 4 * 60 * 60 * 1000L
                if (System.currentTimeMillis() - lastCheck < fourHoursMs) return@launch

                val since = maxOf(lastCheck, System.currentTimeMillis() - 24 * 60 * 60 * 1000L)

                val notifsOn = context.dataStore.data
                    .map { it[booleanPreferencesKey("notifications_enabled")] ?: true }
                    .first()
                if (!notifsOn) return@launch

                val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE)
                context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    "${CallLog.Calls.DATE} > ? AND ${CallLog.Calls.TYPE} = ?",
                    arrayOf(since.toString(), CallLog.Calls.INCOMING_TYPE.toString()),
                    "${CallLog.Calls.DATE} DESC",
                )?.use { cursor ->
                    val numIdx   = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    var notified = 0
                    while (cursor.moveToNext() && notified < 3) {
                        val number = cursor.getString(numIdx) ?: continue
                        val result = checkNumberUseCase(number, isCall = false)
                        if (result.riskLevel == RiskLevel.HIGH || result.riskLevel == RiskLevel.BLOCKED) {
                            CallWarningNotifier.notify(
                                context, number, result.riskLevel,
                                "Son 24 saat içinde gelen şüpheli arama"
                            )
                            notified++
                        }
                    }
                }

                context.dataStore.updateData { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(KEY_LAST_CALLLOG_CHECK, System.currentTimeMillis())
                    }
                }
            } catch (_: SecurityException) {
                // READ_CALL_LOG not granted — silently skip
            }
        }
    }
}
