package com.callshield.app.ui.screen.smsinbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.usecase.sms.ScanInboxUseCase
import com.callshield.app.domain.usecase.sms.ScannedSms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
    private val scanInboxUseCase: ScanInboxUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsInboxUiState())
    val uiState: StateFlow<SmsInboxUiState> = _uiState

    fun scan() {
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
