package com.callshield.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.repository.BlockEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsUiState(
    val totalBlocked: Int = 0,
    val totalSms: Int = 0,
    val totalCalls: Int = 0,
    val weeklyBlocked: Int = 0,
    val dailyBlocked: Int = 0,
    val categoryStats: Map<String, Int> = emptyMap(),
    val riskStats: Map<RiskLevel, Int> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: BlockEventRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.getAll(),
        repository.getWeeklyEvents(),
        repository.getCategoryStats(),
    ) { all, weekly, categories ->
        val oneDayAgo = System.currentTimeMillis() - 24L * 60 * 60 * 1000
        StatsUiState(
            totalBlocked  = all.size,
            totalSms      = all.count { it.type == "SMS" },
            totalCalls    = all.count { it.type == "CALL" },
            weeklyBlocked = weekly.size,
            dailyBlocked  = all.count { it.timestamp >= oneDayAgo },
            categoryStats = categories.toMap(),
            riskStats     = all.groupingBy { it.riskLevel }.eachCount(),
            isLoading     = false,
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )
}
