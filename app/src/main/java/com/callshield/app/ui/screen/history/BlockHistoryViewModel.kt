package com.callshield.app.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.repository.BlockEventRepository
import com.callshield.app.domain.repository.TrustedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryFilter { ALL, SMS, CALL }

@HiltViewModel
class BlockHistoryViewModel @Inject constructor(
    private val repository: BlockEventRepository,
    private val trustedNumberRepository: TrustedNumberRepository,
) : ViewModel() {

    val filter = MutableStateFlow(HistoryFilter.ALL)

    val events = combine(repository.getAll(), filter) { all, f ->
        when (f) {
            HistoryFilter.ALL  -> all
            HistoryFilter.SMS  -> all.filter { it.type == "SMS" }
            HistoryFilter.CALL -> all.filter { it.type == "CALL" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun setFilter(f: HistoryFilter) { filter.value = f }

    /** Göndereni güvenilir listeye ekler (yanlış pozitif düzeltmesi). */
    fun markAsNotSpam(event: BlockEvent) {
        val sender = event.sender.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            trustedNumberRepository.add(phoneNumber = sender, label = "")
        }
    }
}
