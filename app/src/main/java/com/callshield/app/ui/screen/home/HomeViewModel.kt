package com.callshield.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.data.repository.ProtectionRepository
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.repository.BlockEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isProtectionEnabled: Boolean = true,
    val totalBlockedCount: Int = 0,
    val weeklyBlockedCount: Int = 0,
    val recentEvents: List<BlockEvent> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val blockEventRepository: BlockEventRepository,
    private val protectionRepository: ProtectionRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        blockEventRepository.getAll(),
        blockEventRepository.getWeeklyEvents(),
        protectionRepository.isEnabled,
    ) { all, weekly, isActive ->
        HomeUiState(
            isProtectionEnabled = isActive,
            totalBlockedCount   = all.size,
            weeklyBlockedCount  = weekly.size,
            recentEvents        = all.sortedByDescending { it.timestamp }.take(5),
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun toggleProtection() {
        viewModelScope.launch { protectionRepository.toggle() }
    }
}
