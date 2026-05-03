package com.callshield.app.domain.usecase.call

import com.callshield.app.data.repository.IpqsRepository
import com.callshield.app.domain.model.AnalysisResult
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.repository.BlockedNumberRepository
import com.callshield.app.domain.repository.TrustedNumberRepository
import javax.inject.Inject

class CheckNumberUseCase @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository,
    private val trustedNumberRepository: TrustedNumberRepository,
    private val ipqsRepository: IpqsRepository,
    private val analyzePhoneNumber: AnalyzePhoneNumberUseCase,
) {
    suspend operator fun invoke(phoneNumber: String, isCall: Boolean = true): AnalysisResult {
        val reasons = mutableListOf<String>()

        if (trustedNumberRepository.isTrusted(phoneNumber)) {
            return AnalysisResult(riskLevel = RiskLevel.SAFE, reason = "Güvenilir numara listesinde")
        }

        if (blockedNumberRepository.isBlocked(phoneNumber)) {
            reasons.add("Engellenenler listesinde")
            return AnalysisResult(riskLevel = RiskLevel.BLOCKED, reason = reasons.joinToString("; "))
        }

        // Prefix / hat tipi analizi (ücretsiz, anlık)
        val prefixResult = analyzePhoneNumber(phoneNumber)
        if (prefixResult != null) {
            reasons.add(prefixResult.reason)
            // Prefix tek başına BLOCKED seviyesine çıkarmaz, ama IPQS yoksa bu skoru kullan
        }

        val ipqs = ipqsRepository.queryNumber(phoneNumber)
        val riskLevel: RiskLevel = if (ipqs == null) {
            // IPQS yok (free kullanıcı) → prefix sonucunu kullan
            prefixResult?.riskLevel ?: RiskLevel.SAFE
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
        if (prefixResult?.lineType != null && ipqs?.lineType == null) reasons.add("Hat tipi: ${prefixResult.lineType}")

        val category = if (ipqs?.isFraud == true || ipqs?.isSpam == true) "SPAM" else null
        return AnalysisResult(
            riskLevel = riskLevel,
            category = category,
            reason = reasons.joinToString("; ").ifBlank { null },
            score = ipqs?.fraudScore ?: 0,
        )
    }
}
