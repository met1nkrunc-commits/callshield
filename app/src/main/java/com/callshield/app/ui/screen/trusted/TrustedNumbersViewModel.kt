package com.callshield.app.ui.screen.trusted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.repository.TrustedNumber
import com.callshield.app.domain.repository.TrustedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrustedNumbersViewModel @Inject constructor(
    private val repository: TrustedNumberRepository,
) : ViewModel() {

    val numbers = repository.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun add(phoneNumber: String, label: String) {
        if (phoneNumber.isBlank()) return
        viewModelScope.launch { repository.add(phoneNumber.trim(), label.trim()) }
    }

    fun remove(phoneNumber: String) {
        viewModelScope.launch { repository.remove(phoneNumber) }
    }
}
