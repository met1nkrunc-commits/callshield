package com.callshield.app.ui.screen.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.callshield.app.core.util.dataStore
import com.callshield.app.data.local.db.dao.BlockEventDao
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.dao.BlockedSmsDao
import com.callshield.app.data.local.db.dao.NumberQueryCacheDao
import com.callshield.app.data.local.db.dao.PhishingUrlDao
import com.callshield.app.service.NumberUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UpdateState { IDLE, RUNNING, SUCCESS, ERROR }

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val updateState: UpdateState = UpdateState.IDLE,
    val lastUpdateInfo: String = "",
    val blockHighRisk: Boolean = false,
    val isClearing: Boolean = false,
    val clearSuccess: Boolean = false,
)

private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
private val BLOCK_HIGH_KEY    = booleanPreferencesKey("block_high_risk")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockEventDao: BlockEventDao,
    private val blockedNumberDao: BlockedNumberDao,
    private val blockedSmsDao: BlockedSmsDao,
    private val numberQueryCacheDao: NumberQueryCacheDao,
    private val phishingUrlDao: PhishingUrlDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            context.dataStore.data
                .map { it[NOTIFICATIONS_KEY] ?: true }
                .collect { enabled ->
                    _uiState.update { it.copy(notificationsEnabled = enabled) }
                }
        }
        viewModelScope.launch {
            context.dataStore.data
                .map { it[BLOCK_HIGH_KEY] ?: false }
                .collect { enabled ->
                    _uiState.update { it.copy(blockHighRisk = enabled) }
                }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[NOTIFICATIONS_KEY] = enabled }
        }
    }

    fun toggleBlockHighRisk(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[BLOCK_HIGH_KEY] = enabled }
        }
    }

    fun syncNow() {
        if (_uiState.value.updateState == UpdateState.RUNNING) return
        _uiState.update { it.copy(updateState = UpdateState.RUNNING) }

        val request = OneTimeWorkRequestBuilder<NumberUpdateWorker>()
            .addTag("manual_sync")
            .build()

        val wm = WorkManager.getInstance(context)
        wm.enqueue(request)

        viewModelScope.launch {
            wm.getWorkInfoByIdFlow(request.id).collect { info ->
                when (info?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        _uiState.update { it.copy(updateState = UpdateState.SUCCESS, lastUpdateInfo = "Liste güncellendi") }
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        _uiState.update { it.copy(updateState = UpdateState.ERROR, lastUpdateInfo = "Güncelleme başarısız") }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Google Play Data Safety gereksinimi: "Kullanıcı veri silme talebinde bulunabilir."
     * Tüm kişisel veriler (engelleme geçmişi, karantina, cache, phishing log) silinir.
     */
    fun clearAllUserData() {
        if (_uiState.value.isClearing) return
        _uiState.update { it.copy(isClearing = true, clearSuccess = false) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                blockEventDao.deleteAll()
                blockedSmsDao.deleteAll()
                blockedNumberDao.deleteAll()
                numberQueryCacheDao.deleteAll()
                phishingUrlDao.deleteAll()
                context.dataStore.edit { it.clear() }
                _uiState.update { it.copy(isClearing = false, clearSuccess = true) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isClearing = false) }
            }
        }
    }
}
