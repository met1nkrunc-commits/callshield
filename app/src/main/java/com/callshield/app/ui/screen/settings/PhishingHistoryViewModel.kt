package com.callshield.app.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bengel.shared.domain.model.RiskLevel
import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.data.local.db.entity.PhishingUrlEntity
import com.callshield.app.data.repository.PhishingUrlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UrlCheckResult(
    val url: String,
    val riskLevel: RiskLevel,
    val reason: String,
)

@HiltViewModel
class PhishingHistoryViewModel @Inject constructor(
    private val repository: PhishingUrlRepository,
    private val patternMatcher: TurkishPatternMatcher,
) : ViewModel() {

    val urls: StateFlow<List<PhishingUrlEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var urlInput by mutableStateOf("")
        private set

    var checkResult by mutableStateOf<UrlCheckResult?>(null)
        private set

    fun onUrlInputChange(value: String) {
        urlInput = value
        checkResult = null
    }

    fun checkUrl() {
        val url = urlInput.trim()
        if (url.isBlank()) return
        viewModelScope.launch(Dispatchers.Default) {
            val result = patternMatcher.analyze(url, "")
            val risk = result.riskLevel
            val reason = when {
                result.hasPhishingUrl -> "Şüpheli URL tespit edildi"
                result.matchedPatterns.isNotEmpty() -> result.matchedPatterns.take(2).joinToString(", ")
                else -> "Bilinen phishing kalıbı bulunamadı"
            }
            checkResult = UrlCheckResult(url, risk, reason)
            // Save to history if risky
            if (risk == RiskLevel.HIGH || risk == RiskLevel.BLOCKED) {
                repository.insert(url, "Manuel kontrol", url.take(80))
            }
        }
    }
}
