package com.callshield.app.ui.screen.blocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.BlockedNumber
import com.callshield.app.domain.repository.BlockedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlocklistUiState(
    val numbers: List<BlockedNumber> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val repository: BlockedNumberRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlocklistUiState())
    val uiState: StateFlow<BlocklistUiState> = _uiState.asStateFlow()

    init {
        repository.getAll()
            .onEach { list -> _uiState.update { it.copy(numbers = list, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun addNumber(phoneNumber: String, label: String) {
        if (phoneNumber.isBlank()) return
        viewModelScope.launch {
            repository.addNumber(
                BlockedNumber(
                    id = 0,
                    phoneNumber = phoneNumber.trim(),
                    label = label.ifBlank { "Manuel eklendi" },
                    createdAt = System.currentTimeMillis(),
                    isManual = true,
                )
            )
        }
    }

    fun removeNumber(phoneNumber: String) {
        viewModelScope.launch {
            repository.removeNumber(phoneNumber)
        }
    }
}
