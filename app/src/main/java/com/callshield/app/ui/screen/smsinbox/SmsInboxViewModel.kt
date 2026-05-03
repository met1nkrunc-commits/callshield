package com.callshield.app.ui.screen.smsinbox

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.core.util.dataStore
import com.callshield.app.data.repository.PhishingUrlRepository
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.usecase.sms.ScanInboxUseCase
import com.callshield.app.domain.usecase.sms.ScannedSms
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val KEY_SCAN_COUNT     = intPreferencesKey("sms_scan_count")
private val KEY_REVIEW_SHOWN   = booleanPreferencesKey("review_shown")

enum class InboxFilter { ALL, RISKY_ONLY }

data class SmsInboxUiState(
    val messages: List<ScannedSms> = emptyList(),
    val filter: InboxFilter = InboxFilter.ALL,
    val isLoading: Boolean = false,
    val hasScanned: Boolean = false,
    val permissionDenied: Boolean = false,
)

@HiltViewModel
class SmsInboxViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanInboxUseCase: ScanInboxUseCase,
    private val phishingUrlRepository: PhishingUrlRepository,
) : ViewModel() {

    private val urlRegex = Regex("https?://[\\w\\-./?=&%+#@!~:]+")

    /** Emits Unit when the app should show Play In-App Review dialog. */
    private val _triggerReview = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val triggerReview: SharedFlow<Unit> = _triggerReview

    private val _uiState = MutableStateFlow(SmsInboxUiState())
    val uiState: StateFlow<SmsInboxUiState> = _uiState

    fun scan() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, permissionDenied = false) }
        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                try {
                    scanInboxUseCase.execute(limit = 200)
                } catch (_: SecurityException) {
                    null
                }
            }
            if (results == null) {
                _uiState.update { it.copy(isLoading = false, permissionDenied = true) }
            } else {
                withContext(Dispatchers.IO) {
                    // Save phishing URLs from HIGH/BLOCKED messages
                    results.filter {
                        it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.BLOCKED
                    }.forEach { sms ->
                        urlRegex.findAll(sms.message.body).forEach { match ->
                            phishingUrlRepository.insert(match.value, sms.message.address, sms.message.body.take(80))
                        }
                    }
                    // Increment scan count and maybe trigger review
                    val alreadyShown = context.dataStore.data.map { it[KEY_REVIEW_SHOWN] ?: false }.first()
                    if (!alreadyShown) {
                        val newPrefs = context.dataStore.updateData { prefs ->
                            val newCount = (prefs[KEY_SCAN_COUNT] ?: 0) + 1
                            prefs.toMutablePreferences().apply { set(KEY_SCAN_COUNT, newCount) }
                        }
                        val count = newPrefs[KEY_SCAN_COUNT] ?: 0
                        if (count >= 3) {
                            context.dataStore.updateData { prefs ->
                                prefs.toMutablePreferences().apply { set(KEY_REVIEW_SHOWN, true) }
                            }
                            _triggerReview.tryEmit(Unit)
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        messages   = results,
                        isLoading  = false,
                        hasScanned = true,
                    )
                }
            }
        }
    }

    fun setFilter(filter: InboxFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun displayedMessages(state: SmsInboxUiState): List<ScannedSms> = when (state.filter) {
        InboxFilter.ALL       -> state.messages
        InboxFilter.RISKY_ONLY -> state.messages.filter {
            it.riskLevel != RiskLevel.SAFE && it.riskLevel != RiskLevel.LOW
        }
    }
}
