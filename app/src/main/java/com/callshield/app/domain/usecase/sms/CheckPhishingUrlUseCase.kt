package com.callshield.app.domain.usecase.sms

import com.bengel.shared.core.TurkishFraudConstants
import javax.inject.Inject

class CheckPhishingUrlUseCase @Inject constructor() {

    operator fun invoke(text: String): Boolean {
        val normalized = text.lowercase()
        return TurkishFraudConstants.SHORT_URL_PATTERNS.any { it.containsMatchIn(normalized) } ||
               TurkishFraudConstants.SUSPICIOUS_DOMAIN_PATTERNS.any { it.containsMatchIn(normalized) }
    }
}
