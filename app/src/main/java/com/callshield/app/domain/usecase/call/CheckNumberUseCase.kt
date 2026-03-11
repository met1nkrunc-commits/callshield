package com.callshield.app.domain.usecase.call

import com.callshield.app.data.repository.IpqsRepository
import com.callshield.app.domain.model.AnalysisResult
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.repository.BlockedNumberRepository
import javax.inject.Inject

class CheckNumberUseCase @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository,
    private val ipqsRepository: IpqsRepository,
) {
    suspend operator fun invoke(phoneNumber: String, isCall: Boolean = true): AnalysisResult {
        val reasons = mutableListOf<String>()

        if (blockedNumberRepository.isBlocked(phoneNumber)) {
            reasons.add("Engellenenler listesinde")
            return AnalysisResult(riskLevel = RiskLevel.BLOCKED, reason = reasons.joinToString("; "))
        }

        val ipqs = ipqsRepository.queryNumber(phoneNumber)
        val riskLevel: RiskLevel = if (ipqs == null) {
            RiskLevel.SAFE
        } else when {
            ipqs.isFraud -> {
                reasons.add("Dolandırıcılık raporu (IPQS)")
                RiskLevel.BLOCKED
            }
            ipqs.isSpam -> {
                reasons.add("Spam raporu (IPQS)")
                RiskLevel.HIGH
            }
            ipqs.fraudScore >= 85 -> {
                reasons.add("Yüksek dolandırıcılık skoru: ${ipqs.fraudScore}")
                RiskLevel.HIGH
            }
            ipqs.fraudScore >= 60 -> {
                reasons.add("Orta dolandırıcılık skoru: ${ipqs.fraudScore}")
                RiskLevel.MEDIUM
            }
            ipqs.spamScore >= 75 -> {
                reasons.add("Yüksek spam skoru: ${ipqs.spamScore}")
                RiskLevel.MEDIUM
            }
            ipqs.spamScore >= 50 -> {
                reasons.add("Düşük spam skoru: ${ipqs.spamScore}")
                RiskLevel.LOW
            }
            else -> RiskLevel.SAFE
        }

        if (ipqs?.lineType?.lowercase() == "voip") reasons.add("VoIP hattı")

        val category = if (ipqs?.isFraud == true || ipqs?.isSpam == true) "SPAM" else null
        return AnalysisResult(
            riskLevel = riskLevel,
            category = category,
            reason = reasons.joinToString("; ").ifBlank { null },
            score = ipqs?.fraudScore ?: 0,
        )
    }
}
