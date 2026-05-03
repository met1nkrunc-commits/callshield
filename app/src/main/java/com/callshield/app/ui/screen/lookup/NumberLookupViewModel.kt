package com.callshield.app.ui.screen.lookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.core.billing.DailyQuotaManager
import com.callshield.app.core.billing.PlanManager
import com.callshield.app.core.util.BillingConstants
import com.callshield.app.data.local.db.dao.BlockedNumberDao
import com.callshield.app.data.local.db.entity.BlockedNumberEntity
import com.callshield.app.data.repository.IpqsRepository
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.usecase.call.AnalyzePhoneNumberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IpqsDetail(
    val fraudScore: Int,
    val spamScore: Int,
    val carrier: String?,
    val lineType: String?,
    val isFraud: Boolean,
    val isSpam: Boolean,
)

data class LookupResult(
    val number: String,
    val riskLevel: RiskLevel,
    val reason: String,
    val entity: BlockedNumberEntity? = null,
    val ipqsDetail: IpqsDetail? = null,
    val prefixInfo: String? = null,
    val isPremium: Boolean = false,
)

data class NumberLookupUiState(
    val query: String = "",
    val result: LookupResult? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class NumberLookupViewModel @Inject constructor(
    private val blockedNumberDao: BlockedNumberDao,
    private val ipqsRepository: IpqsRepository,
    private val planManager: PlanManager,
    private val analyzePhoneNumber: AnalyzePhoneNumberUseCase,
    private val quotaManager: DailyQuotaManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumberLookupUiState())
    val uiState: StateFlow<NumberLookupUiState> = _uiState

    val remainingQuota = quotaManager.remainingQuota.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyQuotaManager.FREE_DAILY_LIMIT,
    )

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q, result = null) }
    }

    fun lookup() {
        val number = _uiState.value.query.trim().replace(" ", "")
        if (number.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val isPremium = planManager.currentPlan.first() != BillingConstants.PLAN_FREE
            val isBlocked = blockedNumberDao.isBlocked(number)
            val entity = blockedNumberDao.findByNumber(number)
            val prefixResult = analyzePhoneNumber(number)
            val ipqsEntity = if (isPremium) ipqsRepository.queryNumber(number) else null

            val ipqsDetail = ipqsEntity?.let {
                IpqsDetail(
                    fraudScore = it.fraudScore,
                    spamScore  = it.spamScore,
                    carrier    = it.carrier,
                    lineType   = it.lineType,
                    isFraud    = it.isFraud,
                    isSpam     = it.isSpam,
                )
            }

            val riskLevel = when {
                isBlocked            -> RiskLevel.BLOCKED
                ipqsDetail?.isFraud == true  -> RiskLevel.BLOCKED
                ipqsDetail?.isSpam  == true  -> RiskLevel.HIGH
                (ipqsDetail?.fraudScore ?: 0) >= 75 -> RiskLevel.HIGH
                (ipqsDetail?.fraudScore ?: 0) >= 50 -> RiskLevel.MEDIUM
                prefixResult != null         -> prefixResult.riskLevel
                else                         -> RiskLevel.SAFE
            }

            val reasons = mutableListOf<String>()
            if (isBlocked) reasons.add(entity?.reason?.ifBlank { "Dolandırıcı listesinde" } ?: "Dolandırıcı listesinde")
            if (prefixResult != null) reasons.add(prefixResult.reason)
            if (ipqsDetail != null) reasons.add("IPQS skoru: ${ipqsDetail.fraudScore}")

            val result = LookupResult(
                number      = number,
                riskLevel   = riskLevel,
                reason      = reasons.joinToString(" · ").ifBlank { if (riskLevel == RiskLevel.SAFE) "Veritabanımızda kayıtlı değil" else "Şüpheli numara" },
                entity      = entity,
                ipqsDetail  = ipqsDetail,
                prefixInfo  = prefixResult?.reason,
                isPremium   = isPremium,
            )
            _uiState.update { it.copy(result = result, isLoading = false) }
        }
    }
}
