package com.callshield.app.domain.usecase.sms

import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.domain.model.AnalysisResult
import javax.inject.Inject

class AnalyzeSmsContentUseCase @Inject constructor(
    private val patternMatcher: TurkishPatternMatcher,
) {

    operator fun invoke(sender: String, content: String): AnalysisResult {
        val result = patternMatcher.analyze(content, sender)
        val category = patternMatcher.detectCategory(result.matchedPatterns, result.hasPhishingUrl)
        return AnalysisResult(
            riskLevel = result.riskLevel,
            category = category,
            reason = result.matchedPatterns.joinToString(", ").ifBlank { null },
        )
    }
}
