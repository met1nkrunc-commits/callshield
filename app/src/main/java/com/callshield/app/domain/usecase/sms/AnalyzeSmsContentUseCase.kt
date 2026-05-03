package com.callshield.app.domain.usecase.sms

import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.domain.model.AnalysisResult
import com.callshield.app.domain.model.RiskLevel
import javax.inject.Inject

class AnalyzeSmsContentUseCase @Inject constructor(
    private val patternMatcher: TurkishPatternMatcher,
    // Fix #8: DetectBtkCodeUseCase burada inject ediliyor — artık tek yerden çalışıyor.
    private val detectBtkCode: DetectBtkCodeUseCase,
) {

    operator fun invoke(sender: String, content: String): AnalysisResult {
        val result   = patternMatcher.analyze(content, sender)
        val category = patternMatcher.detectCategory(result.matchedPatterns, result.hasPhishingUrl)

        // BTK kodu tespit edilirse risk seviyesi BLOCKED'a yükseltilir.
        val btkCode  = detectBtkCode(content)
        val finalRisk = if (btkCode != null && result.riskLevel != RiskLevel.BLOCKED) {
            RiskLevel.BLOCKED
        } else {
            result.riskLevel
        }

        val reason = buildList {
            if (result.matchedPatterns.isNotEmpty()) add(result.matchedPatterns.joinToString(", "))
            if (btkCode != null) add("BTK spam kodu: $btkCode")
        }.joinToString("; ").ifBlank { null }

        return AnalysisResult(
            riskLevel = finalRisk,
            category  = category,
            reason    = reason,
        )
    }
}
