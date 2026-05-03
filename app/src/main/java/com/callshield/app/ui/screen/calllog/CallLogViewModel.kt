package com.callshield.app.ui.screen.calllog

import android.content.Context
import android.provider.CallLog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.usecase.call.CheckNumberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CallLogEntry(
    val number: String,
    val callType: Int,   // CallLog.Calls.INCOMING_TYPE / OUTGOING_TYPE / MISSED_TYPE
    val date: Long,
    val duration: Long,
    val riskLevel: RiskLevel,
    val reason: String?,
)

data class CallLogUiState(
    val entries: List<CallLogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val permissionDenied: Boolean = false,
    val hasLoaded: Boolean = false,
)

@HiltViewModel
class CallLogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkNumberUseCase: CheckNumberUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallLogUiState())
    val uiState: StateFlow<CallLogUiState> = _uiState

    fun load() {
        _uiState.update { it.copy(isLoading = true, permissionDenied = false) }
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) {
                try {
                    readAndAnalyzeCallLog()
                } catch (_: SecurityException) {
                    null
                }
            }
            if (entries == null) {
                _uiState.update { it.copy(isLoading = false, permissionDenied = true) }
            } else {
                _uiState.update { it.copy(entries = entries, isLoading = false, hasLoaded = true) }
            }
        }
    }

    private suspend fun readAndAnalyzeCallLog(): List<CallLogEntry> {
        val results = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
        )
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null, null,
            "${CallLog.Calls.DATE} DESC",
        )?.use { cursor ->
            val numIdx  = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durIdx  = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            var count   = 0
            while (cursor.moveToNext() && count < 100) {
                val number = cursor.getString(numIdx) ?: ""
                val result = checkNumberUseCase(number, isCall = false)
                results.add(
                    CallLogEntry(
                        number   = number.ifBlank { "Gizli Numara" },
                        callType = cursor.getInt(typeIdx),
                        date     = cursor.getLong(dateIdx),
                        duration = cursor.getLong(durIdx),
                        riskLevel = result.riskLevel,
                        reason   = result.reason,
                    )
                )
                count++
            }
        }
        return results
    }
}
