package com.callshield.app.ui.screen.lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.entity.BlockedNumberEntity
import com.callshield.app.domain.model.RiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LookupResult(
    val number: String,
    val riskLevel: RiskLevel,
    val reason: String,
    val entity: BlockedNumberEntity? = null,
)

data class NumberLookupUiState(
    val query: String = "",
    val result: LookupResult? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class NumberLookupViewModel @Inject constructor(
    private val blockedNumberDao: BlockedNumberDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumberLookupUiState())
    val uiState: StateFlow<NumberLookupUiState> = _uiState

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q, result = null) }
    }

    fun lookup() {
        val number = _uiState.value.query.trim().replace(" ", "")
        if (number.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val isBlocked = blockedNumberDao.isBlocked(number)
            val result = if (isBlocked) {
                // Grab the entity for extra detail (reason/category)
                val entity = blockedNumberDao.findByNumber(number)
                LookupResult(
                    number    = number,
                    riskLevel = RiskLevel.BLOCKED,
                    reason    = entity?.reason?.ifBlank { "Dolandırıcı veritabanında kayıtlı" }
                        ?: "Dolandırıcı veritabanında kayıtlı",
                    entity    = entity,
                )
            } else {
                LookupResult(
                    number    = number,
                    riskLevel = RiskLevel.SAFE,
                    reason    = "Bu numara veritabanımızda bulunmuyor",
                )
            }
            _uiState.update { it.copy(result = result, isLoading = false) }
        }
    }
}
